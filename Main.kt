import java.lang.Character.UnicodeScript
import java.lang.Character.UnicodeBlock
import java.lang.Character.UnicodeBlock.*
import javax.naming.directory.SearchResult
import kotlin.text.CharDirectionality.*
import kotlin.text.CharCategory.*

val ignored = Node.main()
val Char.seq get() = Node.leaves.getOrDefault(this, "")
val String.escapedHTML get()=replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;")

//Ensure every single-character ID within the document is unique
val    SEQ_ID= CharSequence::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}}
val  BLOCK_ID= UnicodeBlock::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SEQ_ID}
val SEARCH_ID= SearchResult::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SEQ_ID+BLOCK_ID}
val CATGRY_ID= CharCategory::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SEQ_ID+BLOCK_ID+SEARCH_ID}
val SCRIPT_ID=UnicodeScript::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}+SEQ_ID+BLOCK_ID+SEARCH_ID+CATGRY_ID}
val DIRCTN_ID=CharDirectionality::class.simpleName!!.uppercase().last{it!in CharDirectionality.entries.map{it.code}  +BLOCK_ID+SEARCH_ID+CATGRY_ID+SCRIPT_ID+SEQ_ID}

//These blocks should be condensed into a singular character for display since they are either extremely large (performance) or homogenous (redundant)
val CONDENSED=setOf(CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,CJK_UNIFIED_IDEOGRAPHS,HANGUL_SYLLABLES,HIGH_SURROGATES,HIGH_PRIVATE_USE_SURROGATES,LOW_SURROGATES,PRIVATE_USE_AREA)

//selects all options when associated label is interacted with
val FN="for(var g of document.getElementById(this.htmlFor).children)for(var o of g.children)o.selected=true"
val JS="onclick='$FN' onkeypress='$FN'"

val html = StringBuilder("""<!DOCTYPE html>
<meta charset=UTF-8>
<title>How do I type...?</title>
<link rel=icon href='data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -8 9 9" font-size="8"><text>⌨️</text><text>❓</text></svg>'>
<style>:root{color-scheme:dark light;>body{margin:0;>form{
>search{ direction:rtl; overflow-y:auto; float:left; position:sticky; top:0; height:100vh; margin-right:1px;
	>nav{ direction:initial; width:100%; margin-bottom:1mm; position:sticky; top:0; background-color:canvas;
		>button{ position:relative; font-size:xx-large; aspect-ratio:1; width:calc(100%/3); contain:strict; box-shadow:0 0 0 1px inset canvas;
			&:after{ position:absolute; font-size:initial; right:0; bottom:0; content:'📥' }
			&:first-child:after{ margin:auto; left:0; height:70%; content:'❓' }
		}
	}
	>input{ direction:initial; position:sticky; top:2em; margin-right:1em; float:right; font-size:xx-large; width:1em; height:1em; box-shadow:0 0 0 1px canvas}
	>label{ direction:initial; text-align:center; font-size:large; font-weight:bold; display:block; width:100%; padding-top:1mm; white-space:pre;
		&:first-of-type{ text-align:right; font-size:revert; font-weight:revert; padding-top:revert }
		&:is(:target,:active,:hover,:focus)+select>optgroup>option:not(:checked){ background-color:highlight }
	}
	>select{ direction:initial; display:block; width:100%; overflow-y:auto; text-align-last:justify;
		option{ padding-right:1mm }
		&:last-child{ text-align-last:initial; font-size:xx-small }
	}
}>main{ overflow-y:auto; line-height:0; font-size:0;
	>section{ contain:strict; display:inline;
		>span{ contain:strict;
			>button{ contain:strict; font-size:xx-large; width:2em; height:2em; margin:1px;
				&:target,&:active,&:hover,&:focus{ z-index:2; position:relative }
				&:before,&:after{
					contain:strict;
					content:attr($SCRIPT_ID)' 'attr($DIRCTN_ID)' 'attr($CATGRY_ID)' ';
					text-align-last:justify;
					font-size:xx-small;
					position:absolute;
					white-space:pre;
					margin:auto;
					width:100%;
					bottom:0;
					right:0;
					left:0;
					top:0;
				}
				&:after{
					content:attr($SEQ_ID);
					top:revert;
					height:1em;
					display:flex;
					align-items:flex-end;
					justify-content:center;
					text-align-last:center
				}
				&:disabled:after{ content:'🚧' }
			}
		}
	}
}}}}${(CharDirectionality.entries.map{it.code}.toSet()+CharCategory.entries.map{it.code}+UnicodeScriptFamily.values().flatMap{it}.map{it.code}).joinToString(""){"""
search#$SEARCH_ID:has(#$it:not(:checked))+main>section>span.$it,"""}}${UnicodeBlockGroup.values().flatMap{it}.joinToString(""){"""
search#$SEARCH_ID:has(#_${it.id}:not(:checked))+main>section#${it.cssID},"""}}
search#$SEARCH_ID:has(#$SEQ_ID:checked)+main>section>span>button:not([$SEQ_ID]),
search#$SEARCH_ID:has(#$SEQ_ID:indeterminate)+main>section>span>button[$SEQ_ID]{display:none}
</style>
<form>
<search id=$SEARCH_ID>
	<nav>
		<button title=About formaction=https://github.com/RyanHilbert/US-Universal-Keyboard-Layout#readme>📖</button
		><button title='Windows Download' formaction=https://github.com/RyanHilbert/US-Universal-Keyboard-Layout/releases/download/0.2/KbdEditInstallerUSX.exe>🪟</button
		><button title='KbdEdit Download' formaction=/kbdedit.kld>⌨️</button>
	</nav>
	<input type=checkbox checked id=$SEQ_ID title=Typeable? onchange=if(this.checked){if(this.multiple)this.indeterminate=!(this.multiple=this.checked=false)}else{this.multiple=true}><label for=$SEQ_ID>Typeable?</label>
	<label for=$CATGRY_ID tabindex=0 $JS>Category</label>
	<select id=$CATGRY_ID multiple size=${CharCategory.entries.size+CharCategory.entries.map{it.code.first()}.toSet().size}>${CharCategory.entries.map{when(val c=it.code.first()){'C'->'L' else->c}}.toSet().plus('C').joinToString(""){ g->"""
		<optgroup label=${CharCategory.entries.first{it.code.startsWith(g)}.name.substringAfterLast('_',"Other").lowercase().capitalize()}>"""+CharCategory.entries.drop(1).plus(UNASSIGNED).filter{it.code.startsWith(g)}.joinToString(""){"""
			<option selected id=${it.code} label=${it.label}>"""}}}
	</select>
	<label for=$SCRIPT_ID tabindex=0 $JS>Script	</label>
	<select id=$SCRIPT_ID multiple size=${UnicodeScriptFamily.values().sumOf{it.size+1}}>${UnicodeScriptFamily.values().joinToString(""){g->"""
		<optgroup label=${g.label}>"""+g.joinToString(""){"""
			<option selected id=${it.code} label=${it.label}>"""}}}
	</select>
	<label for=$DIRCTN_ID tabindex=0 $JS>Direction	</label>
	<select id=$DIRCTN_ID multiple size=${CharDirectionality.entries.map{it.code}.toSet().size+CharDirectionality.entries.map{it.strength}.toSet().size}>${CharDirectionality.entries.drop(1).map{it.strength}.toSet().joinToString(""){g->"""
		<optgroup label=$g>"""+CharDirectionality.entries.drop(1).plus(UNDEFINED).filter{it.strength==g&&it<=LEFT_TO_RIGHT_EMBEDDING}.joinToString(""){"""
			<option selected id=${it.code} label=${it.label}>"""}}}
	</select>
	<label for=$BLOCK_ID tabindex=0 $JS>Block	</label>
	<select id=$BLOCK_ID multiple size=${UnicodeBlockGroup.values().sumOf{it.size+1}}>${UnicodeBlockGroup.values().joinToString(""){g->"""
		<optgroup label=${g.label}>"""+g.joinToString(""){"""
			<option selected id=_${it.id} label=${it.label}>"""}}}
	</select>
</search>
<main>
<section id=${Char.MIN_VALUE.block.id}>
<span class='${Char.MIN_VALUE.script.code} ${Char.MIN_VALUE.direction.code} ${Char.MIN_VALUE.category.code}'>
""")
fun main(){
	var category = Char.MIN_VALUE.category.code
	var direction = Char.MIN_VALUE.direction.code
	var script = Char.MIN_VALUE.script.code
	var block = Char.MIN_VALUE.block
	for(char in Char.MIN_VALUE..Char.MAX_VALUE)if(char.script!=UnicodeScript.UNKNOWN||char.block==null||char.block in CONDENSED){
		val seq = char.seq
		val q = if(seq.isEmpty())"" else " "+SEQ_ID+"="+seq.escapedHTML
		val b = char.block
		val c = char.category.code
		val d = char.direction.code
		val s = char.script.code
		val n = char.name
		val id = char.id
		if(b != block) html.append("</span></section><section id=${b.id}><span class='$s $d $c'>")
		else if(c!=category||d!=direction||s!=script)html.append("</span><span class='$s $d $c'>")
		if(b in CONDENSED){
			if(char == b.chars.first())
				html.append("<button disabled $SCRIPT_ID=$s $DIRCTN_ID=$d $CATGRY_ID=$c title='U+$id-U+${b.chars.last.id} ${b.toString().replace('_',' ')}' formaction=#${b.id} id=$id>&#x$id</button\n>")
		}else html.append("<button $SCRIPT_ID=$s $DIRCTN_ID=$d $CATGRY_ID=$c$q title='U+$id $n' formaction=#$id id=$id>&#x$id</button\n>")
		block=b; category=c; direction=d; script=s
	}
	html.setLength(html.length-"</button\n>".length)
	print(html)
	System.err.print(Node.toDeadTableString())
}
