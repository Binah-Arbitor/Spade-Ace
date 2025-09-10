import 'package:flutter_test/flutter_test.dart';
import 'package:spade_ace/main.dart';

void main() {
  testWidgets('App loads correctly', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const SpadeAceApp());

    // Verify that app title is displayed
    expect(find.text('Spade Ace - Decryption Tool'), findsOneWidget);
    
    // Verify main sections are present
    expect(find.text('File Selection'), findsOneWidget);
    expect(find.text('Auto-Detection Results'), findsOneWidget);
    expect(find.text('Manual Override'), findsOneWidget);
    expect(find.text('Attack Configuration'), findsOneWidget);
    expect(find.text('Decryption Progress'), findsOneWidget);
  });
}