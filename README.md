# Google contacts vCard picture fixer

A _very_ simple project to fix contacts exported from Google by replacing photo links with base64 encoded images.

### Motivation

It turns out a lot of mail/calendar/_contacts_ providers doesn't support VCards that has link as photo causing contacts missing picture after import

### Building

To build project simply use maven:
```bash
mvn assembly:single
```

### Usage

```bash
java -jar target/google-contacts-vcard-picture-fixer-1.0-SNAPSHOT-jar-with-dependencies.jar [input_file] ([output_file])
```

* `[input_file]` - file with contacts to be processed
* `[output_file]` (optional) - file where processed contacts will be stored (if not provided output will be stored in same location as input with `_output` suffix)