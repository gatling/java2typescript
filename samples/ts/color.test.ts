import chalk from "chalk";

import {
    Stream,
    Arrays,
  } from "./j2ts/jdk-types";


export function test() {

    let b = "hello jjs";

    // java.util.List is typed as a JS array by java2ts: cast to reach the Java methods
    let a = <any>Arrays.asList( "item1", "item2", "item3", "item4.1" );

    print( chalk.red(b) );

    a.stream().forEach( (e:string) => {
      print( chalk.green(e) );
    });

    Stream.of<string>( "<item2>" ).forEach( e => {
      print( chalk.green(e) );
    });

}
