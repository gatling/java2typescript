//
// STARTUP
//

// Calling Thread.getContextClassLoader() from JS fails on GraalJS 25 / JDK 25
// ("Attempt to lookup caller-sensitive method using restricted lookup object"),
// so load jvm-npm.js with the system class loader instead.
load({
  name: './target/js/jvm-npm.js',
  script: String(new java.lang.String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get('./target/js/jvm-npm.js'))))
    .replace('Thread.currentThread().getContextClassLoader()', 'java.lang.ClassLoader.getSystemClassLoader()')
});

print( typeof require );

require.paths = [
  "node_emul"
];

java.lang.System.setProperty( 'jvm-npm.debug', 'false');

var process = {
  argv:'',
  platform:'macosx',
  env: {
    TERM:'color'
  },
  stderr:undefined,
  stdout:undefined
}

load('./target/js/main.js');
