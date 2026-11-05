//
// Type-check the TypeScript sources (tsc --noEmit -p .)
//
// Errors located in the generated `ts/j2ts/` files are only reported as warnings:
// those files are produced by the java2ts processor (`mvn package`) and must not be edited,
// and TypeScript has no option to skip checking a single `.ts` file (`skipLibCheck` only covers `.d.ts`).
// (`skipLibCheck` in tsconfig.json also hides the same kind of error in the generated `jdk.d.ts`.)
// Any other error fails the build.
//
import { spawnSync } from 'node:child_process';
import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';

const tsc = join(dirname(createRequire(import.meta.url).resolve('typescript/package.json')), 'bin', 'tsc');

const { stdout, stderr, status, error } = spawnSync(process.execPath, [tsc, '--noEmit', '--pretty', 'false', '-p', '.'], { encoding: 'utf8' });
if (error) throw error;
process.stderr.write(stderr);

// group each diagnostic with its (indented) continuation lines
const diagnostics = stdout.split('\n').reduce((acc, line) => {
  if (/^\s/.test(line) && acc.length) acc[acc.length - 1] += '\n' + line;
  else if (line) acc.push(line);
  return acc;
}, []);

const generated = diagnostics.filter(d => d.replaceAll('\\', '/').startsWith('ts/j2ts/'));
const others = diagnostics.filter(d => !generated.includes(d));

if (generated.length) {
  console.warn(`WARNING: ${generated.length} type error(s) in generated ts/j2ts/ files (ignored):`);
  generated.forEach(d => console.warn('  ' + d));
}
others.forEach(d => console.error(d));

process.exit(others.length || (status !== 0 && !generated.length) ? 1 : 0);
