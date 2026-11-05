## HOW RUN SAMPLE

This examples use "[GraalJs](https://github.com/graalvm/graaljs)"

> Run it on a GraalVM 25 JDK to benefit from runtime compilation. On other JDKs, GraalJS runs in interpreter-only mode (see [Runtime Optimization Support](https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support)).

### install javascript packages

> npm install

### Generate TypeScript stuff

> mvn clean package

This task will generate `jdk.d.ts` and `jdk-types.ts` into folder `ts/j2ts`

### Compile typescript

> npm run build

This task use [parcel](https://parceljs.org/) to bundle all modules in a single javascript file in `target/js/main.js`

### Run sample

> npm start
