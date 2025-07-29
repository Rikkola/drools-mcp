# DRL Test Files

This directory contains Drools Rule Language (DRL) files used by the DRLToolTest class.

## Files Description

### Valid DRL Files

- **valid-simple-rule.drl** - Basic valid DRL structure with a simple rule
- **person-adult-check.drl** - Complex DRL with Person type declaration and adult checking logic
- **empty-rule.drl** - Rule that never fires due to eval(false) condition
- **order-discount-logic.drl** - Business logic for order processing with discount rules
- **person-age-categorization.drl** - Categorizes persons by age (minor, adult, senior)
- **count-facts.drl** - Rule that counts total facts in working memory
- **message-with-quotes.drl** - Tests JSON escaping with quotes in string content
- **counter-increment.drl** - Demonstrates multiple rule firings with counter increment

### Invalid DRL Files

- **invalid-syntax.drl** - Contains invalid Drools syntax for error testing
- **invalid-syntax-2.drl** - Alternative invalid syntax for different test scenarios

## Usage

These files are loaded by the `DRLToolTest.readDRLFile(String filename)` method and used in various test scenarios to validate DRL compilation, execution, and error handling.

## File Naming Convention

- Use descriptive names that indicate the purpose or business logic
- Use hyphens to separate words
- Include `.drl` extension
- Prefix with `invalid-` for files containing syntax errors
