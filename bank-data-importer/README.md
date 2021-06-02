Bank data importer
====

Compilation
-----------
```
mvn spring-boot:build-image
```

Run
---
```
docker run -v $(pwd)/conf:/conf -v $(pwd)/data:/data:ro docker.io/xaviermichel/bank-data-importer:latest --google.sheet.spreadsheetId="xxx-xxx-xxx"
```

Tips
----

How to retrieve gsheet models and generate a non optimized reflect config for sprint native ?

```
unzip -l google-api-services-sheets-v4-rev20210504-1.31.0.jar | grep '/model' | awk '{print $4}' | sed 's/.class$//' | tr '/' '.' | while read l; do echo "{\"name\": \"$l\", \"allDeclaredFields\": true, \"allPublicConstructors\": true},"; done
```

