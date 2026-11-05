
import { marked, Renderer, type Tokens } from "marked";

class WikiRenderer extends Renderer {

    langs = {
        'actionscript3' :true,
        'bash'          :true,
        'csharp'        :true,
        'coldfusion'    :true,
        'cpp'           :true,
        'css'           :true,
        'delphi'        :true,
        'diff'          :true,
        'erlang'        :true,
        'groovy'        :true,
        'java'          :true,
        'javafx'        :true,
        'javascript'    :true,
        'perl'          :true,
        'php'           :true,
        'none'          :true,
        'powershell'    :true,
        'python'        :true,
        'ruby'          :true,
        'scala'         :true,
        'sql'           :true,
        'vb'            :true,
        'html'          :true,
        'xml'           :true
    };

	paragraph({ tokens }:Tokens.Paragraph) { return this.parser.parseInline(tokens) + '\n\n'; }

    html({ text }:Tokens.HTML|Tokens.Tag) { return text; }

	heading({ tokens, depth }:Tokens.Heading) { return 'h' + depth + '. ' + this.parser.parseInline(tokens) + '\n\n' }

	strong({ tokens }:Tokens.Strong) { return '*' + this.parser.parseInline(tokens) + '*' }

	em({ tokens }:Tokens.Em) { return '_' + this.parser.parseInline(tokens) + '_' }

	del({ tokens }:Tokens.Del) { return '-' + this.parser.parseInline(tokens) + '-' }

	codespan({ text }:Tokens.Codespan) { return '{{' + text + '}}' }

	blockquote({ tokens }:Tokens.Blockquote) { return '{quote}' + this.parser.parse(tokens) + '{quote}' }

	br() { return '\n' }

	hr() { return '----' }

	link({ href, title, tokens }:Tokens.Link) {
		let arr = [this.parser.parseInline(tokens),href];
		if (title) arr.push(title);

		return '[' + arr.join('|') + ']'
	}

	list({ items, ordered }:Tokens.List) {
        let body = items.map( (item) => this.listitem(item) ).join('');
        let arr = body.trim().split('\n').filter( (line) => line );

		var type = ordered ? '#' : '*'
        return arr.map( (line) => type + ' ' + line ).join('\n') + '\n\n'

	}

	listitem({ tokens }:Tokens.ListItem) { return this.parser.parse(tokens) + '\n' }

	image({ href }:Tokens.Image) { return '!' + href + '!'}

	table({ header, rows }:Tokens.Table) {
		let head = this.tablerow({ text: header.map( (cell) => this.tablecell(cell) ).join('') });
		let body = rows.map( (row) => this.tablerow({ text: row.map( (cell) => this.tablecell(cell) ).join('') }) ).join('');

		return head + body + '\n'
	}

	tablerow({ text }:Tokens.TableRow) { return text + '\n' }

	tablecell(cell:Tokens.TableCell) {
		var type = cell.header ? '||' : '|'
		return type + this.parser.parseInline(cell.tokens);
	}

	code({ text, lang }:Tokens.Code) {
		lang = (<any>this.langs)[lang] ? lang :  "";

		return '{code:' + lang + '}\n' + text + '\n{code}\n\n';
	}

    text(token:Tokens.Text|Tokens.Escape): string {
        return ('tokens' in token && token.tokens) ? this.parser.parseInline(token.tokens) : token.text;
    }
}

const  renderer = new WikiRenderer()

export function markdown2wiki(md:string) {
	return marked(md.toString(), {
        renderer: renderer,
        async: false
    });
}


export function test() {
    let res = markdown2wiki( '# TEST' );

    print( res );
}

