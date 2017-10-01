import java.lang.Character.*
import java.util.*
import java.util.Collections.*

open class Node(var id:Char=MIN_VALUE,map:MutableMap<Char,Node> =mutableMapOf()):MutableMap<Char,Node>by map{
	operator fun invoke(f:Node.()->Unit):Node{f();return this}
	override fun hashCode() = id.toInt()
	override fun toString():String{
		val str = hex
		return if(str[0].isLetter()||str[0].isDigit()&&str[1].isDigit()&&str[2].isDigit()&&str[3].isDigit())str else '"'+str+'"'
	}
	val leaf:Leaf?get(){
		var node:Node? = this
		while(node!is Leaf && node!=null) node = node.values.firstOrNull()
		return node as Leaf?
	}
	open fun recursivelySetIDs(ids:MutableSet<Char> =((1.toChar()..0xFFFF.toChar())-id).toMutableSet()){
		for((char,node)in this) if(node.id == MIN_VALUE){
			node.id = if(char in ids) char else ids.last()
			ids -= node.id
			node.recursivelySetIDs(ids)
		}
	}
	open fun toGraphvizString(visited:MutableSet<Node> =mutableSetOf()):String{
		var result = ""
		for((char,node)in this){
			result+="$this->$node[label=${if(char.isLetterOrDigit())"$char" else "\"${char.toString().replace("\\","\\\\").replace("\"","\\\"")}\""}]\n"
			if(node !in visited){
				visited += node
				result += node.toGraphvizString(visited)
			}
		}
		return result
	}
	open fun toDeadTableString(visited:MutableSet<Node> =mutableSetOf()):String{
		var result = "DEADKEY "+hex+"\n"
		for((char,node)in this)result+=char.hex+' '+node.hex+if(node!is Leaf && node!is Root)"@\n" else "\n"
		result += '\n'
		for(node in values)if(node !in visited){
			visited += node
			result += node.toDeadTableString(visited)
		}
		return result
	}
	infix operator fun StringBuilder.rangeTo(char:Char):Node{//maps all permutations to char
		fun permute(i:Int){
			if(i<=0)this as CharSequence..char
			else for(j in 0..i){
				val c = this[j]
				this[j] = this[0]
				this[0] = c
				permute(i-1)
			}
		}
		permute(length-1)
		return this@Node
	}
	infix operator fun CharSequence.rangeTo(char:Char):Node{
		var node = this@Node
		for(i in 0..length-2){
			if(node is Leaf)throw IllegalStateException("$this -> $char attempted to overwrite ${substring(0,i)} -> $node")
			node = node.getOrPut(this[i]){Node()}
		}
		val leaf = Leaf(char)
		val removed = node.put(this[length-1],leaf)
		if(null!=removed && leaf!=removed)throw IllegalStateException("$this -> $char attempted to overwrite $this -> ${removed.leaf}")
		return this@Node
	}
	infix operator fun CharSequence.rangeTo(chars:Pair<Char,Char>):Node{
		if(leaves[chars.first.toInt()]&&leaves[chars.second.toInt()])return this@Node
		var nodes = setOf(this@Node)
		for(i in 0..length-2){
			val set:MutableSet<Node> = mutableSetOf()
			val n = Node()
			for(node in nodes)if(node !is Leaf){
				set += node.getOrPut(this[i].toUpperCase()){n}
				set += node.getOrPut(this[i].toLowerCase()){n}
			}
			if(set.isEmpty())throw IllegalStateException("$this -> ${chars.first} attempted to overwrite ${substring(0,1)} -> ${nodes.first()}")
			nodes = set
		}
		val leaf1 = Leaf(chars.first)
		val leaf2 = if(chars.first == chars.second) leaf1 else Leaf(chars.second)
		for(node in nodes)if(node !is Leaf){
			nodes = EMPTY_SET as Set<Node>
			val removed1 = node.put(this[length-1].toUpperCase(),leaf1)
			val removed2 = node.put(this[length-1].toLowerCase(),leaf2)
			if(null!=removed1 && leaf1!=removed1)throw IllegalStateException("$this -> $leaf1 attempted to overwrite $this -> $removed1")
			if(null!=removed2 && leaf2!=removed2)throw IllegalStateException("$this -> $leaf2 attempted to overwrite $this -> $removed2")
		}
		if(nodes != EMPTY_SET)throw IllegalStateException("$this -> ${chars.first} attempted to overwrite $this -> ${nodes.firstOrNull()}")
		return this@Node
	}



	class Leaf(val char:Char):Node(char,EMPTY_MAP as MutableMap<Char,Node>){
		init{leaves.set(char.toInt())}
		override fun toString() = if(char.isLetterOrDigit())""+char else "\""+char+"\""
		override fun equals(o:Any?) = if(o is Leaf) char == o.char else false
		override fun toGraphvizString(visited:MutableSet<Node>) = ""
		override fun toDeadTableString(visited:MutableSet<Node>) = ""
		override fun put(key:Char,value:Node):Node?=try{
			super.put(key,value)
		}catch(e:UnsupportedOperationException){
			throw IllegalStateException("$key -> $value attempted to overwrite $this",e)
		}
	}



	companion object Root:Node('⎄'){
		override fun toString() = "⎄"
		override fun toGraphvizString(visited:MutableSet<Node>) = "digraph{\n"+super.toGraphvizString(visited)+'}'
		override fun toDeadTableString(visited:MutableSet<Node>) = "﻿DEADTABLE\n\n"+super.toDeadTableString(visited)+"ENDDEADTABLE"
		val leaves = BitSet(1+MAX_VALUE.toInt())
		init{
			leaves.set('⎄'.toInt())
		}
		val CHARS=(0xFF.toChar()..0x33FF.toChar())+(0xA500.toChar()..0xABFF.toChar())+(0xF900.toChar()..0xFFFF.toChar())
		val Any.hex:String get() = String.format("%04X",hashCode())
		val Char.name get() = getName(toInt())?:""
		val Char.nfd get() = java.text.Normalizer.normalize(""+this,java.text.Normalizer.Form.NFD)
		val Char.nfkd get() = java.text.Normalizer.normalize(""+this,java.text.Normalizer.Form.NFKD)
		operator infix fun Char.plus(c:Char) = this to c
		operator fun Char.unaryPlus() = this + this
		operator fun CharSequence.unaryPlus() = StringBuilder(this)
		fun CharSequence.isPrintableASCII():Boolean{
			for(c in this) if(c <' ' || c >'~') return false
			return true
		}
		fun CharSequence.hasLetterOrDigit():Boolean{
			for(c in this) if(c.isLetterOrDigit()) return true
			return false
		}
		fun CharSequence.isLettersAndDigits():Boolean{
			for(c in this) if(!c.isLetterOrDigit()) return false
			return true
		}
		val CharSequence.sub get() = StringBuilder(this).apply{
			for(i in 0 until length) this[i] = this[i].sub
		}
		val Char.sub get() = when(this){
			'̈'->'"'//diaeresis
			'̄'->'-'//macron
			//breve
			'̨'->','//ogonek
			'́'->'\''//acute accent
			'̂'->'^'//circumflex accent
			'̇'->'*'//dot above
			//caron
			'̧'->','//cedilla
			'̃'->'~'//tilde
			'·'->'.'//middle dot
			'̋'->'\''//double acute
			'̊'->'0'//ring above
			//horn
			'̀'->'`'//grave
			'̏'->'`'//double grave
			//inverted breve
			//comma below

			//'̥'->'0'//ring below
			'̣'->'.'//dot below
			'̱'->'_'//macron below
			'̭'->'^'//circumflex accent below
			//'̰'->'~'//tilde below
			//breve below
			'̤'->':'//diaeresis below
			//hook above
			'̓'->','//comma above
			'̔'->','//reversed comma above

			'̳'->'='//double low line

			'̅'->'-'//overline
			'⁄'->'/'//fraction slash
			'̸'->'/'//long solidus overlay
			else->this
		}
		fun String.removeAffixes() = this
			.removeSuffix(" CROSS")
			.removeSuffix(" LATIN")
			.removeSuffix(" WHITE")//shadowed white latin cross
			.removeSuffix(" SYRIAC")
			.removeSuffix(" SOURCE")
			.removeSuffix(" CONSTANT")
			.removeSuffix(" ORNAMENT")
			.removeSuffix(" SYMBOL")
			.removeSuffix(" SIGN")
			.removeSuffix(" MARK")
			.removeSuffix(" ACCENT")
			.removeSuffix(" LINE")//property line, wavy line, centre line symbol
			.removeSuffix(" SUIT")//playing cards
			.removeSuffix(" CROSS")
			.removeSuffix(" COPRODUCT")//amalgamation or coproduct
			.removeSuffix(" PRODUCT")//wreath product
			.removeSuffix(" PROOF")//end of proof
			.removeSuffix(" ANGLE")//right angle
			.removeSuffix(" WAVE")//sine wave
			.removeSuffix(" OR")//amalgamation or coproduct
			.removeSuffix(" OF")//element of
			.removeSuffix(" TO")//identical to
			.removeSuffix(" NOTES")
			.removeSuffix(" NOTE")
			.removePrefix("CROSS ")

			.removePrefix("OF ")
			.removePrefix("BEAMED ")
			.removePrefix("MUSIC ")
			.removePrefix("SYMBOL ")
			.removePrefix("FOR ")//for all
			.removePrefix("THERE ")//there exists
			.removePrefix("APL ")
			.removePrefix("FUNCTIONAL ")
			.removePrefix("IDEOGRAPHIC ")
			.removePrefix("TELEGRAPH ")
			.removePrefix("LOGICAL ")
			.removePrefix("CHESS ")
			.removePrefix("DEGREE ")//celsius fahrenheit



		@JvmStatic fun main(vararg args:String){
			for(c in CHARS-'Ȩ'-'ȩ'){//map accented characters
				val decomp = c.nfd
				if(decomp.length < 2) continue
				val sub = decomp.sub
				if(!sub.isPrintableASCII()||!sub.hasLetterOrDigit()) continue
				//if(sub.length < 3) println("$sub -> $c") else println(sub.reversed().toString()+" -> $c")
				if(sub.length < 3) +sub..c else{
					//sub.reversed()..c
					""+sub[1]+sub[2]+sub[0]..c//TODO: ADD SUPPORT FOR SWITCHING ACCENT ORDER
				}
			}
			for(c in "㍷㎜㎝㎞") c.nfkd+'1'..c
			for(c in CHARS-('Ⅰ'..'ⅿ')-('⒑'..'⒛')-"ﬀﬅ㍷㎜㎝㎞㏂…⩶︙︰".asIterable()){//map ligatures
				val s = c.nfkd
				if(s.length > 1 && s.isPrintableASCII()) s..c
			}
			"am"..'㏂'
			"ft"..'ﬅ'
			+"2f"..'ﬀ'
			"SP"..'␠'
			"BEL"..'␇'
			"<3"..'❤'
			"3>"..'❥'
			"(:"..'☻'
			":)"..'☺'
			":("..'☹'
			"):"..'☹'

			"/0"..'∅'

			"+-"..'±'
			"-+"..'∓'
			+".+"..'∔'
			"/\\"..'∧'
			"\\/"..'∨'

			"||"..'∥'
			"/||"..'∦'
			//"::"..'∷'
			//"-."..'∸'
			//"-:"..'∹'
			":-:"..'∺'
			//+":~"..'∻'

			"~~"..'≈'
			"/~~"..'≉'

			+".="..'≐'
			//"..="..'≑'

			":="..'≔'
			"=:"..'≕'
			"=o"..'≖'
			"o="..'≗'

			+"*="..'≛'

			+"d="..'≝'
			+"m="..'≞'
			+"?="..'≟'
			+"/="..'≠'
			+"-="..'≡'
			"/-="..'≢'
			//"=="..'≣'
			"<-"..'≤'
			">-"..'≥'
			"<="..'≦'
			">="..'≧'
			"</="..'≨'
			">/="..'≩'
			"<<"..'≪'
			">>"..'≫'
			+")("..'≬'

			"/<"..'≮'
			"/>"..'≯'
			"/-<"..'≰'
			"/->"..'≱'
			+"<~"..'≲'
			+">~"..'≳'
			"/~<"..'≴'
			"/~>"..'≵'
			"<>"..'≶'
			"><"..'≷'

			"{-".."≼"
			"}-"..'≽'
			"{~"..'≾'
			"}~"..'≿'

			"0+"..'⊕'
			"0-"..'⊖'
			"0x"..'⊗';"0X"..'⊗'
			"0/"..'⊘'
			"0."..'⊙'
			"0o"..'⊚';"0O"..'⊚';"00"..'⊚'
			"0*"..'⊛'
			"0="..'⊜'
			"0_"..'⊝'
			"[+"..'⊞'
			"[-"..'⊟'
			"[x"..'⊠';"[X"..'⊠'
			"[."..'⊡'

			"<|"..'⊲'
			"|>"..'⊳'
			"=<|"..'⊴'
			"=|>"..'⊵'

			"|X"..'⋉';"|x"..'⋉'
			"X|"..'⋊';"x|"..'⋊'

			"=||"..'⋕'
			+"<."..'⋖'
			+".>"..'⋗'

			"-<"..'⋜'
			"->"..'⋝'

			"SUM"..+'∑'
			"PROD"..+'∏'
			"COPROD"..+'∐'
			"SQRT"..+'√'
			"CBRT"..+'∛'
			"FORT"..+'∜'
			"1S"..+'∫'
			"2S"..+'∬'
			"3S"..+'∭'
			"4S"..+'⨌'
			"1DS"..+'∮'
			"2DS"..+'∯'
			"3DS"..+'∰'
			"CS"..+'∱'
			"C1DS"..+'∲'
			"A1DS"..+'∳'

			"EIGHTH"..'♫'+'♪'
			"MAN"..'⛂'+'⛀'
			"MEN"..'⛃'+'⛁'
			"BULLET"..'•'+'◦'
			"BOWTIE"..'⧓'+'⋈'
			"LOZENGE"..'⧫'+'◊'
			"RHOMBUS"..'◆'+'◇'
			"SNOWMAN"..'⛇'+'☃'
			"SPARKLE"..'❈'+'❇'
			"SPARKLY"..+'✨'
			"TIME"..+'⌛'

			for(c in CHARS.asReversed()-'⅌'-'⍴'-'₠'-'∷'-'⏥')with(c.name.removeAffixes()){if(isNotEmpty()&&' ' !in this&&'-' !in this&&c.nfkd.length<=1)this..+c}

			for(b in CHARS-'◆'){
				val name = b.name
				if(!name.startsWith("BLACK "))continue
				val bname = name.substring(6).removeAffixes()
				if(' ' in bname)continue
				for(w in CHARS-'◇'){
					val name = w.name
					if(!name.startsWith("WHITE "))continue
					val wname = name.substring(6).removeAffixes()
					if(wname==bname){
						wname..b+w
						break
					}
				}
			}
			recursivelySetIDs()
			print(toGraphvizString())
			//print(toDeadTableString())
		}
	}
}
//fun main(vararg args:String){Node.Root.main(*args)}
