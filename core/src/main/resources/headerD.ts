/*
 * Project: java2typescript - https://github.com/bsorrentino/java2typescript
 *
 * Author: bsorrentino 
 *
 * TYPESCRIPT DEFINITIONS
 *
 */

type int    = number;
type long   = number;
type float	= number;
type double = number;
type byte   = number;
type char   = string;

type chararray = char[];
type bytearray = byte[];

declare namespace java.lang {

	interface Class<T> {}
	interface AutoCloseable {}
	interface Cloneable {}

	type Object = any;
}

declare namespace java.util {

	interface RandomAccess {}

	type List<E> = E[];

	type Map<K extends keyof any, V> = Record<K, V>;
}

declare namespace java.io {

	interface Closeable {}
	interface Serializable {}
}

//
// Nashorn compatibility
//

declare function print( ...args: any[] ):void

declare function load( module:string ):void

declare namespace Java {

  export function type<T>( t:string):T;

  export function from<T>( list:java.util.List<T> ):Array<T> ;
  
}

//
// Generated declarations
//

