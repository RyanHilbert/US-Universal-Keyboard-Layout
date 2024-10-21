import java.lang.Character.UnicodeScript
import java.lang.Character.UnicodeScript.*
import kotlin.text.CharDirectionality.*
import kotlin.text.CharCategory.*

val DISABLED=java.util.EnumSet.of(HANGUL,HAN,UNKNOWN) // large scripts which impact performance

val ignored = Node.main()
val String.escapedHTML get() = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
val Char.seq get() = Node.leaves.getOrDefault(this, "")
val SEQ_ID='J'
val SCRIPT_ID=     UnicodeScript::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}}
val CATGRY_ID=      CharCategory::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SCRIPT_ID}
val DIRCTN_ID=CharDirectionality::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SCRIPT_ID+CATGRY_ID}

val html = StringBuilder("""<!DOCTYPE html>
<meta charset=UTF-8>
<title>How do I type...?</title>
<link rel=icon href='data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -4 5 5" font-size="4"><text>⌨️</text></svg>'>
<style>:root{color-scheme:dark light;>body{margin:0;
>nav>form>button{
	z-index:1;
	width:2em;
	height:2em;
	position:fixed;
	right:0;
	top:0;
	bottom:0;
	margin:auto;
	font-size:xxx-large;
	clip-path:circle();
	&:first-child{bottom:revert}
	&:last-child{top:revert}
	&:after{
		content:'';
		width:100%;
		height:100%;
		position:absolute;
		top:-50%;
		left:-50%;
		right:-50%;
		bottom:-50%;
		margin:auto;
		border-radius:50%;
		border:solid buttonborder;
	}
}>search{
	float:left;
	position:sticky;
	top:0;
	height:100vh;
	overflow-y:auto;
	direction:rtl;
	>*{direction:initial}
	>label{
		display:block;
		text-align-last:justify;
		&:not(:first-child):before{content:'Filter'}
		&:not(:first-child):has(>:checked):before{content:'Include'}
		>input{
			width:1em;
			height:1em;
			float:left
		}
		>select{
			width:100%;
			display:block;
			overflow-y:auto;
			option{padding-right:.5ch}
		}
		&:after{
			content:' ';
			float:right;
			white-space:pre
		}
		&:last-child option{
			font-size:xx-small;
			text-align-last:initial
		}
	}
}>form{
	overflow-y:auto;
	line-height:0;
	font-size:0;
	>button{
		contain:strict;
		font-size:xx-large;
		width:2em;
		height:2em;
		&:hover,&:target,&:focus{
			position:relative;
			z-index:2
		}
		&:before,&:after{
			content:attr($SEQ_ID);
			text-align-last:center;
			font-size:xx-small;
			position:absolute;
			margin:auto;
			width:100%;
			bottom:0;
			right:0;
			left:0
		}
		&:before{
			content:attr($SCRIPT_ID)' 'attr($DIRCTN_ID)' 'attr($CATGRY_ID)' ';
			text-align-last:justify;
			white-space:pre;
			top:0
		}
	}
}}}
body:has(#$SEQ_ID:checked)>form>button:not([$SEQ_ID]),
${      CharDirectionality.entries.map{it.code}.toSet().joinToString(",\n"){"body:has(#$DIRCTN_ID:not(:checked)):has(#$it:not(:checked))>form>button[$DIRCTN_ID=$it]"}},
${UnicodeScriptFamily.values().flatMap{it}.map{it.code}.joinToString(",\n"){"body:has(#$SCRIPT_ID:not(:checked)):has(#$it:not(:checked))>form>button[$SCRIPT_ID=$it]"}},
${                    CharCategory.entries.map{it.code}.joinToString(",\n"){"body:has(#$CATGRY_ID:not(:checked)):has(#$it:not(:checked))>form>button[$CATGRY_ID=$it]"}}{display:none}
${                    CharCategory.entries.map{it.code}.joinToString(",\n"){"body:has(#$CATGRY_ID:checked):has(#$it:checked)>form>button[$CATGRY_ID=$it]"}},
${UnicodeScriptFamily.values().flatMap{it}.map{it.code}.joinToString(",\n"){"body:has(#$SCRIPT_ID:checked):has(#$it:checked)>form>button[$SCRIPT_ID=$it]"}},
${      CharDirectionality.entries.map{it.code}.toSet().joinToString(",\n"){"body:has(#$DIRCTN_ID:checked):has(#$it:checked)>form>button[$DIRCTN_ID=$it]"}}{display:revert}
</style>
<nav><form>
	<button title=About formaction=https://github.com/RyanHilbert/US-Universal-Keyboard-Layout#readme>📖</button>
	<button title='Windows Download' formaction=https://github.com/RyanHilbert/US-Universal-Keyboard-Layout/releases/download/0.2/KbdEditInstallerUSX.exe>🪟</button>
	<button title='Mac Download (coming soon)'>🍎</button>
</form></nav>
<search>
	<label><input type=checkbox id=$SEQ_ID> Composable</label><br>
	<label><input type=checkbox id=$CATGRY_ID> Category
		<select multiple size=${CharCategory.entries.size+CharCategory.entries.map{it.code.first()}.toSet().size}>${CharCategory.entries.map{when(val c=it.code.first()){'C'->'L' else->c}}.toSet().plus('C').joinToString(""){g->"""
			<optgroup label=${CharCategory.entries.first{it.code.startsWith(g)}.name.substringAfterLast('_',"Other").lowercase().capitalize()}>"""+CharCategory.entries.drop(1).plus(UNASSIGNED).filter{it.code.startsWith(g)}.joinToString(""){"""
				<option selected id=${it.code} label=${it.label}>"""}}}
		</select>
	</label>
	<label><input type=checkbox id=$SCRIPT_ID> Script
		<select multiple size=${UnicodeScriptFamily.values().sumOf{it.size+1}}>${UnicodeScriptFamily.values().joinToString(""){g->"""
			<optgroup label=${g.label}>"""+g.joinToString(""){"""
				<option ${if(it in DISABLED)"disabl" else "select"}ed id=${it.code} label=${it.label}>"""}}}
		</select>
	</label>
	<label><input type=checkbox id=$DIRCTN_ID> Direction
		<select multiple size=${CharDirectionality.entries.map{it.code}.toSet().size+CharDirectionality.entries.map{it.strength}.toSet().size}>${CharDirectionality.entries.drop(1).map{it.strength}.toSet().joinToString(""){g->"""
			<optgroup label=$g>"""+CharDirectionality.entries.drop(1).plus(UNDEFINED).filter{it.strength==g&&it<=LEFT_TO_RIGHT_EMBEDDING}.joinToString(""){"""
				<option selected id=${it.code} label=${it.label}>"""}}}
		</select>
	</label>
	<label><input type=checkbox disabled>🚧Block🚧
		<select multiple disabled>${UnicodeBlockGroup.values().joinToString(""){g->"""
			<optgroup label=${g.label}>"""+g.joinToString(""){"""
				<option label=${it.label}>"""}}}
		</select>
	</label>
</search>
<form>""")
fun main() {
	for (char in Char.MIN_VALUE..Char.MAX_VALUE) if (char.script !in DISABLED) {
		val seq = char.seq
		val q = if(seq.isEmpty())"" else " "+SEQ_ID+"="+seq.escapedHTML
		val c = char.category.code
		val d = char.direction.code
		val s = char.script.code
		val n = char.name
		val id = char.id
		html.append("<button $SCRIPT_ID=$s $DIRCTN_ID=$d $CATGRY_ID=$c$q title='U+$id $n' formaction=#$id id=$id>&#x$id</button\n>")
	}
	println(html.append("</form>"))
}
