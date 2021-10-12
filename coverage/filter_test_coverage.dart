import 'dart:io';

void main() {
  printMessage('Start filtering the lcov.info file');
  final file = File('coverage/lcov.info');
  if (!file.existsSync()) {
    printMessage('"lcov.info" does not exist');
    return;
  }
  const endOfRecord = 'end_of_record';
  final sections = <LcovSection>[];
  final lines = file.readAsLinesSync();
  LcovSection? currentSection;
  lines.forEach((line) {
    if (line.endsWith('.dart')) {
      final filePath = line.replaceAll('SF:', '');
      currentSection = LcovSection()
        ..header = line
        ..filePath = filePath;
    } else if (line == endOfRecord) {
      final session = currentSection;
      if (session != null) {
        session.footer = line;
        sections.add(session);
      }
    } else {
      currentSection?.body.add(line);
    }
  });
  final filteredSections = getFilteredSections(sections);
  final sb = StringBuffer();
  filteredSections.forEach((section) {
    sb.write(section.toString());
  });
  file.writeAsStringSync(sb.toString());
  printMessage('Filtered the lcov.info file');
}

class LcovSection {
  String? filePath;
  String? header;
  final body = <String>[];
  String? footer;

  String getBodyString() {
    final path = filePath;
    if (path == null) {
      throw ArgumentError('file path can not be null');
    }
    final file = File(path);
    final content = file.readAsLinesSync();
    final sb = StringBuffer();
    getFilteredBody(body, content).forEach((item) => sb
      ..write(item)
      ..write('\n'));
    return sb.toString();
  }

  @override
  String toString() {
    return '$header\n${getBodyString()}$footer\n';
  }
}

List<LcovSection> getFilteredSections(List<LcovSection> sections) {
  return sections.where((section) {
    if (section.header?.endsWith('.g.dart') == true) {
      return false;
    } else if (section.header?.endsWith('dummy_service.dart') == true) {
      return false;
    } else if (section.header?.startsWith('SF:lib/vendor/') == true) {
      return false;
    } else if (section.header?.startsWith('SF:lib/util/locale') == true) {
      return false;
    }
    return true;
  }).toList();
}

List<String> getFilteredBody(List<String> body, List<String> lines) {
  return body.where((line) {
    if (line.startsWith('DA:')) {
      final sections = line.split(',');
      final lineNr = int.parse(sections[0].replaceAll('DA:', ''));
      final callCount = int.parse(sections[1]);
      if (callCount == 0) {
        final fileLine = lines[lineNr - 1].trim();
        if (excludedLines.contains(fileLine)) {
          return false;
        }
        for (final line in excludedStartsWithLines) {
          if (fileLine.trim().startsWith(line)) {
            return false;
          }
        }
      }
    }
    return true;
  }).toList();
}

const excludedLines = <String>[];

const excludedStartsWithLines = <String>[];

void printMessage(String message) {
  // ignore: avoid_print
  print(message);
}
