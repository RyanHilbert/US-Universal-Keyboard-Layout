import java.awt.*
import java.awt.datatransfer.*
import java.awt.event.*
import java.lang.Character.*
import java.lang.Character.UnicodeScript.*
import java.nio.charset.StandardCharsets.*
import java.text.Normalizer.*
import java.util.Collections.*
import javax.swing.*
import javax.swing.JFileChooser.*
import javax.swing.JFrame.*
import javax.swing.ScrollPaneConstants.*
import javax.swing.filechooser.*
import javax.swing.plaf.basic.*

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
		var result = "DEADKEY "+hex+"\r\n"
		for((char,node)in this)result+=char.hex+' '+node.hex+if(node!is Leaf && node!is Companion)"@\r\n" else "\r\n"
		result += "\r\n"
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
		val removed = try{
			node.put(this[length-1],leaf)
		}catch(e:IllegalStateException){
			throw IllegalStateException(substring(0,lastIndex)+e.message,e)
		}
		if(null!=removed && leaf!=removed)throw IllegalStateException("$this -> $char attempted to overwrite $this -> ${removed.leaf}")
		leaves.put(char,this.toString())
		return this@Node
	}
	infix operator fun CharSequence.rangeTo(chars:Pair<Char,Char>):Node{
		if(chars.first in leaves.keys && chars.second in leaves.keys)return this@Node
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
		if(nodes !== EMPTY_SET)throw IllegalStateException("$this -> ${chars.first} attempted to overwrite $this -> ${nodes.firstOrNull()}")
		leaves.put(chars.second,toString().toLowerCase())
		leaves.put(chars.first,toString().toUpperCase())
		return this@Node
	}



	class Leaf(val char:Char):Node(char,EMPTY_MAP as MutableMap<Char,Node>){
		override fun toString() = if(char.isLetterOrDigit())""+char else "\""+char+"\""
		override fun equals(other:Any?) = if(other is Leaf) char == other.char else false
		override fun toGraphvizString(visited:MutableSet<Node>) = ""
		override fun toDeadTableString(visited:MutableSet<Node>) = ""
		override fun put(key:Char,value:Node):Node?=try{
			super.put(key,value)
		}catch(e:UnsupportedOperationException){
			throw IllegalStateException("$key -> $value attempted to overwrite ${leaves[char]} -> $this",e)
		}
	}



	companion object:Node('⎄'){
		override fun toString() = "⎄"
		override fun toGraphvizString(visited:MutableSet<Node>) = "digraph{\n"+super.toGraphvizString(visited)+'}'
		override fun toDeadTableString(visited:MutableSet<Node>) = "﻿DEADTABLE\r\n\r\n"+super.toDeadTableString(visited)+"ENDDEADTABLE"

		val leaves = mutableMapOf<Char,String>()//maps each character to a key sequence for visual display
		val KEYBOARD=((' '..'~')+('Α'..'Ω')+('α'..'ω')+('←'..'↙')-'΢').toSet()
		val CHARS=((0xA0.toChar()..MAX_VALUE).filter{
			it.script in setOf(COMMON,LATIN,GREEK) && "CJK" !in it.block.toString() && "ANA" !in it.block.toString()
		}-KEYBOARD).toSet()

		fun Any?.toTitleString() = toString().toLowerCase().split('_').map{s->if(s.length>2)s.capitalize()else s}.joinToString(" ")
		val Any.hex:String get() = String.format("%04X",hashCode())
		val Char.name get() = getName(toInt())?:""
		val Char.block get() = UnicodeBlock.of(this)
		val Char.script get() = UnicodeScript.of(this.toInt())
		val Char.nfd get() = normalize(""+this,Form.NFD)
		val Char.nfkd get() = normalize(""+this,Form.NFKD)
		val Char.direction get() = try{directionality}catch(e:IllegalArgumentException){null}
		operator infix fun Char.plus(c:Char) = this to c
		operator fun Char.unaryPlus() = this + this
		operator fun CharSequence.unaryPlus() = StringBuilder(this)
		fun String.containsWord(word:String,ignoreCase:Boolean=false,vararg delimiters:Char) = word.isBlank()||equals(word,ignoreCase)||delimiters.any{startsWith(word+it,ignoreCase)||endsWith(it+word,ignoreCase)||contains(it+word+it,ignoreCase)}
		fun CharSequence.isPrintableASCII():Boolean{
			for(c in this) if(c <' ' || c >'~') return false
			return true
		}
		fun CharSequence.hasLetterOrDigit():Boolean{
			for(c in this) if(c.isLetterOrDigit()) return true
			return false
		}
		fun CharSequence.isKeyboardLettersAndNotEntirelyDigits():Boolean{
			var letter = false
			for(c in this){
				if(!c.isLetterOrDigit()||c!in KEYBOARD) return false
				if(c.isLetter()) letter = true
			}
			return letter
		}
		val CharSequence.sub get() = StringBuilder(this).apply{
			for(i in 0 until length) this[i] = this[i].sub
		}as CharSequence
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
		//'̋'->'\''//double acute
		//'̊'->'0'//ring above
		//horn
			'̀'->'`'//grave
		//'̏'->'`'//double grave
		//inverted breve
		//comma below

		//'̥'->'0'//ring below
			'̣'->'.'//dot below
			'̱'->'_'//macron below
		//'̭'->'^'//circumflex accent below
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
			.removeSuffix(" ET")//tironian sign et
			.removeSuffix(" CLOCK")//alarm clock & timer clock
			.removeSuffix(" CROSS")
			.removeSuffix(" LATIN")
			.removeSuffix(" WHITE")//shadowed white latin cross
			.removeSuffix(" SYRIAC")
			.removeSuffix(" SOURCE")
			.removeSuffix(" CONSTANT")
			.removeSuffix(" ORNAMENT")
			.removeSuffix(" SYMBOL")
			.removeSuffix(" MARK")
			.removeSuffix(" SIGN")
			.removeSuffix(" FOR")
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
			.removeSuffix(" MOON")//first quarter moon
			.removeSuffix(" QUARTER")//last quarter moon
			.removeSuffix(" BALL")//soccer ball
			.removeSuffix(" PUMP")//fuel pump
			.removeSuffix(" NODE")//ascending descending
			.removeSuffix(" PIECE")//shogi piece
			.removeSuffix(" LOOP")
			.removeSuffix(" CURLY")

			.removePrefix("UNIVERSAL ")//universal recycling symbol
			.removePrefix("JAPANESE ")//japanese bank symbol
			.removePrefix("PLACE ")//place of interest sign
			.removePrefix("PARTIAL ")//partial differential
			.removePrefix("MONOGRAM ")
			.removePrefix("DIGRAM ")
			.removePrefix("HOT ")//beverage springs
			.removePrefix("ICE ")//ice skate
			.removePrefix("CROSS ")
			.removePrefix("STAR ")
			.removePrefix("STAFF ")
			.removePrefix("AND ")
			.removePrefix("OF ")
			.removePrefix("FUNERAL ")//funeral urn
			.removePrefix("HEADSTONE ")//headstone graveyard symbol
			.removePrefix("BEAMED ")
			.removePrefix("MUSIC ")
			.removePrefix("MAP ")//map symbol for lighthouse
			.removePrefix("SYMBOL ")
			.removePrefix("FOR ")//for all
			.removePrefix("THERE ")//there exists
			.removePrefix("APL ")
			.removePrefix("FUNCTIONAL ")
			.removePrefix("LOGICAL ")
			.removePrefix("CHESS ")
			.removePrefix("DEGREE ")//celsius fahrenheit
			.removePrefix("SYMBOL ")
			.removePrefix("EARTH ")//earth ground
			.removePrefix("HIGH ")//high voltage sign
			.removePrefix("PERMANENT ")//permanent paper sign
			.removePrefix("SUMMATION ")//summation top & bottom



		open @JvmStatic fun main(vararg args:String){
			for(c in CHARS-'Ȩ'-'ȩ'){//map accented characters
				val decomp = c.nfd
				if(decomp.length < 2) continue
				val sub = decomp.sub
				if(!sub.isPrintableASCII()||!sub.hasLetterOrDigit()) continue
				if(sub.length < 3){
					sub..c
					if(c!in "ĀāÁá")sub.reversed()..c
				}else{
					val r = sub.reversed()
					r..c
					if(c!in "ǕṺǖṻ")r[1]+(r[0]+r.substring(2))..c
				}
			}
			for(c in "㍷㎜㎝㎞") c.nfkd+'1'..c
			for(c in CHARS-('Ⅰ'..'ⅿ')-"ﬀﬅ㍷㎜㎝㎞㏂…⩶︙︰".asIterable()){//map ligatures
				val s = c.nfkd.sub
				if(s.length > 1 && s.isKeyboardLettersAndNotEntirelyDigits()) s..c
			}
			"OE"..'Œ'
			"oe"..'œ'
			"AE"..'Æ'
			"ae"..'æ'
			"-AE"..'Ǣ'
			"-ae"..'ǣ'
			"'AE"..'Ǽ'
			"'ae"..'ǽ'

			"am"..'㏂'
			"ft"..'ﬅ'
			+"2f"..'ﬀ'
			"SP"..'␠'
			"BEL"..'␇'

			+"$$"..'₪'
			+"a$"..'؋'
			+"A$"..'₳'
			+"B$"..'₿'
			+"B|"..'฿'
			+"C$"..'₡'
			+"C|"..'₵'
			+"c$"..'¢'
			+"D$"..'₯'
			+"d$"..'₫'
			+"E$"..'€'
			+"e$"..'₠'
			+"F$"..'₣'
			+"f$"..'ƒ'
			+"G$"..'₲'
			+"K$"..'₭'
			+"L$"..'₺'
			+"M$"..'₼'
			+"m$"..'₥'
			+"N$"..'₦'
			+"P$"..'₱'
			+"r$"..'₢'
			+"S$"..'₷'
			+"Z$"..'₴'
			+"T$"..'₮'
			+"t$"..'₶'
			+"W$"..'₩'
			+"Y$"..'¥'
			+"?$"..'֏'
			+"&$"..'₰'

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
			"SAND"..+'⌛'

			for(c in CHARS-'⍴'-'∷'-'⏥'-'⅌')with(c.name.removeAffixes()){if(isNotEmpty()&&' ' !in this&&'-' !in this&&c.nfkd.length<=1)this..+c}

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
			val deadtable = toDeadTableString()
			val graphviz = toGraphvizString()
			clear()//allow the node graph to be garbage collected
			with(object:JFrame("US-Universal Character Map"){}){//GUI setup
				defaultCloseOperation = EXIT_ON_CLOSE
				ToolTipManager.sharedInstance().apply{
					initialDelay = 0
					dismissDelay = Int.MAX_VALUE
				}
				val filer = JFileChooser()
				val txt = FileNameExtensionFilter("Text Documents","txt")
				val kld = FileNameExtensionFilter("KLD Files","kld")
				var visibleButtonCount = 0
				var buttonSize = 0
				contentPane.add(JScrollPane(JPanel().apply{
					layout = FlowLayout().apply{
						alignment = FlowLayout.LEFT
						hgap = 0
						vgap = 0
					}
					val resized = object:ComponentAdapter(){
						override fun componentResized(e:ComponentEvent?){
							val width = this@with.size.width-buttonSize
							val buttonsPerColumn = width/buttonSize
							val rows = (visibleButtonCount-1)/buttonsPerColumn+1
							val height = rows*buttonSize
							this@apply.preferredSize = Dimension(width,height)
						}
					}
					this@with.addComponentListener(resized)
					val clipboard = object:MouseAdapter(){
						override fun mouseClicked(e:MouseEvent){
							Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(e.component.name),null)
						}
					}
					val label = JLabel()
					val search = JTextField()
					val untypable = JCheckBox("",true)
					val scripts = mutableMapOf<UnicodeScript,JCheckBox>()
					val blocks = mutableMapOf<UnicodeBlock,JCheckBoxMenuItem>()
					val categories = mutableMapOf<CharCategory,JCheckBoxMenuItem>()
					val directions = mutableMapOf<CharDirectionality?,JCheckBoxMenuItem>()
					for(c in(CHARS union KEYBOARD).sorted()) add(JButton(""+c).apply{
						scripts.getOrPut(c.script){JCheckBox("",true)}
						blocks.getOrPut(c.block){JCheckBoxMenuItem("",true)}
						categories.getOrPut(c.category){JCheckBoxMenuItem("",true)}
						directions.getOrPut(c.direction){JCheckBoxMenuItem("",true)}
						name = text
						buttonSize = preferredSize.height
						preferredSize = Dimension(buttonSize,buttonSize)
						margin = Insets(0,0,0,0)
						if(!font.canDisplay(c))font=GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts.find{it.canDisplay(c)}?.deriveFont(font.size2D)?:font
						val seq = leaves[c]
						toolTipText =
							if(seq==null)"U+"+c.hex+' '+c.name
							else if(c.name.contains(seq,true))"<HTML>U+"+c.hex+' '+c.name.replace(seq,"<B><U>$seq</U></B>",true)
							else "<HTML>U+"+c.hex+" <B><U>"+seq+"</U></B> "+c.name
						++visibleButtonCount
						addMouseListener(clipboard)
						if(c!in KEYBOARD && c!in leaves.keys) isEnabled = false
					})
					val update = object:AbstractAction(){
						override fun actionPerformed(e:ActionEvent?){
							visibleButtonCount = 0
							for(component in this@apply.components){
								val c = component.name.first()
								val keywords = search.text.split(' ')
								component.isVisible=if(!component.isEnabled&&!untypable.isSelected||!scripts[c.script]!!.isSelected||!blocks[c.block]!!.isSelected||!categories[c.category]!!.isSelected||!directions[c.direction]!!.isSelected||keywords.any{!c.name.containsWord(it,true,' ','-')})false
								else{
									++visibleButtonCount
									true
								}
							}
							label.text = ""+visibleButtonCount+" / "+(CHARS union KEYBOARD).size
							resized.componentResized(null)
						}
					}
					this@with.contentPane.add(JMenuBar().apply{
						add(JMenu("Export").apply{
							add(JMenuItem(object:AbstractAction("Dead Keys to Clipboard"){
								override fun actionPerformed(e:ActionEvent?){
									Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(deadtable),null)
								}
							}))
							add(JMenuItem(object:AbstractAction("Graphviz to Clipboard"){
								override fun actionPerformed(e:ActionEvent?){
									Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(graphviz),null)
								}
							}))
							add(JMenuItem(object:AbstractAction("Dead Keys to File..."){
								override fun actionPerformed(e:ActionEvent?){
									filer.fileFilter = kld
									if(filer.showSaveDialog(this@with)==APPROVE_OPTION) filer.selectedFile.writeText(deadtable,UTF_16LE)
								}
							}))
							add(JMenuItem(object:AbstractAction("Graphviz to File..."){
								override fun actionPerformed(e:ActionEvent?){
									filer.fileFilter = txt
									if(filer.showSaveDialog(this@with)==APPROVE_OPTION) filer.selectedFile.writeText(graphviz)
								}
							}))
						})
						val all = object:AbstractAction("All"){
							override fun actionPerformed(e:ActionEvent){
								val source = e.source as AbstractButton
								val isSourceSelected = source.isSelected
								for(component in source.parent.components) if(component is AbstractButton) component.isSelected = isSourceSelected
								update.actionPerformed(e)
							}
						}
						fun JCheckBoxMenuItem.disableCloseOnClick(){ui=object:BasicCheckBoxMenuItemUI(){override fun doClick(m:MenuSelectionManager?){doClick(0)}}}
						add(JMenu("Directionalities").apply{
							add(JCheckBoxMenuItem("",true).apply{
								action = all
								disableCloseOnClick()
							})
							for((directionality,box)in directions){
								box.action = update
								box.text = directionality.toTitleString()
								box.disableCloseOnClick()
								add(box)
							}
						})
						add(JMenu("Categories").apply{
							add(JCheckBoxMenuItem("",true).apply{
								action = all
								disableCloseOnClick()
							})
							for((category,box)in categories){
								box.action = update
								box.text = category.toTitleString()
								box.disableCloseOnClick()
								add(box)
							}
						})
						add(JMenu("Blocks").apply{
							add(JCheckBoxMenuItem("",true).apply{
								action = all
								disableCloseOnClick()
							})
							for((block,box)in blocks){
								box.action = update
								box.text = block.toTitleString()
								box.font = box.font.deriveFont(box.font.size*3/4f)
								box.disableCloseOnClick()
								add(box)
							}
						})
						for((script,box)in scripts){
							box.action = update
							box.text = script.toString().toLowerCase().capitalize()
							add(box)
						}
						label.text = ""+visibleButtonCount+" / "+(CHARS union KEYBOARD).size
						search.action = update
						untypable.action = update
						untypable.text = "Untypable"
						add(untypable)
						add(search)
						add(label)
					},BorderLayout.NORTH)
				},VERTICAL_SCROLLBAR_AS_NEEDED,HORIZONTAL_SCROLLBAR_NEVER).apply{verticalScrollBar.unitIncrement=buttonSize},BorderLayout.CENTER)
				val screenSize = Toolkit.getDefaultToolkit().screenSize
				size = Dimension(screenSize.width/2,screenSize.height/2)//reasonable default size, since pack() will not work in this case
				isVisible = true
			}
		}
	}
}
