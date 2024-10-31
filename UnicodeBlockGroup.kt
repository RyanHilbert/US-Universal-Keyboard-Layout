/**Defines groups of UnicodeBlocks and extension methods related to Characters and Strings*/
import java.lang.Character.UnicodeScript.UNKNOWN
import java.lang.Character.UnicodeBlock
import java.lang.Character.UnicodeBlock.*
import kotlin.text.CharDirectionality.*
import kotlin.text.CharCategory.UNASSIGNED

val Char.block: UnicodeBlock? get() = of(this)
val UnicodeBlock?.group: UnicodeBlockGroup get() = UnicodeBlockGroup.values().first{this in it}
val UnicodeBlock?.chars: CharRange get() = UnicodeBlockGroup[this]!!
val UnicodeBlock?.id: String get() = chars.first().id.dropLast(1)
val UnicodeBlock?.cssID: String get() = when(val c=id.first()){
	in '0'..'9' -> """\3$c ${id.drop(1)}"""
	else -> id
}
val Char.id get()=String.format("%04X", this.code)
val Char.name: String get() = Character.getName(code)?:""

val Char.direction: CharDirectionality? get() = try{directionality} catch(_:IllegalArgumentException){null}
val Enum<*>?.strength: String get() = when(this){
	LEFT_TO_RIGHT,RIGHT_TO_LEFT,RIGHT_TO_LEFT_ARABIC -> "Strong"
	EUROPEAN_NUMBER,EUROPEAN_NUMBER_SEPARATOR,EUROPEAN_NUMBER_TERMINATOR,ARABIC_NUMBER,COMMON_NUMBER_SEPARATOR,NONSPACING_MARK,BOUNDARY_NEUTRAL -> "Weak"
	PARAGRAPH_SEPARATOR,SEGMENT_SEPARATOR,WHITESPACE,OTHER_NEUTRALS -> "Neutral"
	else -> "Other"
}
val Enum<*>?.title: String get()=if(this==null)UNKNOWN.title else name.split('_').joinToString(" "){it.lowercase().capitalize()}.replace(" To ","-to-").replace("Cjk","CJK")
val CharCategory.label: String get() = "'${title.replaceFirst(Regex("""\s+\w*$"""),"").replace(' ',' ')} ${code.superscript}'"
val CharDirectionality?.label: String get() = when(this){
	UNDEFINED -> "'${UNKNOWN.title} ${code.superscript}'"
	null,in LEFT_TO_RIGHT_EMBEDDING..CharDirectionality.entries.last()->"'Explicit ${code.superscript}'"
	else->"'${title.replace("Number ","№ ").replace("European №","🇪🇺 №").replace("n № Separator","n № Sep.").replace(' ',' ')} ${code.superscript}'"
}
val CharDirectionality?.code: Char get() = when(this){
	UNDEFINED -> 'Z'
	LEFT_TO_RIGHT -> 'L'
	RIGHT_TO_LEFT -> 'R'
	RIGHT_TO_LEFT_ARABIC -> 'A'
	EUROPEAN_NUMBER -> 'E'
	EUROPEAN_NUMBER_SEPARATOR -> 'U'
	EUROPEAN_NUMBER_TERMINATOR -> 'T'
	ARABIC_NUMBER -> 'N'
	COMMON_NUMBER_SEPARATOR -> 'C'
	NONSPACING_MARK -> 'M'
	BOUNDARY_NEUTRAL -> 'B'
	PARAGRAPH_SEPARATOR -> 'P'
	SEGMENT_SEPARATOR -> 'S'
	WHITESPACE -> 'W'
	OTHER_NEUTRALS -> 'O'
	else -> 'X'
}
val UnicodeBlock?.label get() = if(this==null)UNASSIGNED.title else "'"+toString()
	.replace("UNIFIED_","")
	.replace("ABORIGINAL_","")
	.replace("MISCELLANEOUS","MISC")
	.replace("MATHEMATICAL","MATH")
	.replace("S_EXTENDED","S_EXT")
	.replace("S_SUPPLEMENT","S_SUPP")
	.replace("L_MARK","L")
	.replace("ACTERS","S")
	.split('_')
	.joinToString(" "){when(it){"CJK","IPA"->it "AND","FOR"->it.lowercase()else->it.lowercase().capitalize()}}
	.replace(" and Months","&Months").replace(Regex(""" \w( |$)""")){it.value.replaceFirst(' ','-')}+"'"

enum class UnicodeBlockGroup(vararg blocks:UnicodeBlock?):Set<UnicodeBlock?>by setOf(*blocks){
	BICAMERAL(BASIC_LATIN,LATIN_1_SUPPLEMENT,LATIN_EXTENDED_A,LATIN_EXTENDED_B,IPA_EXTENSIONS,SPACING_MODIFIER_LETTERS,COMBINING_DIACRITICAL_MARKS,GREEK,CYRILLIC,CYRILLIC_SUPPLEMENTARY,ARMENIAN),
	RIGHT_TO_LEFT(HEBREW,ARABIC,SYRIAC,ARABIC_SUPPLEMENT,THAANA,NKO,SAMARITAN,MANDAIC,SYRIAC_SUPPLEMENT,ARABIC_EXTENDED_B,ARABIC_EXTENDED_A),
	BRAHMIC(DEVANAGARI,BENGALI,GURMUKHI,GUJARATI,ORIYA,TAMIL,TELUGU,KANNADA,MALAYALAM,SINHALA,THAI,LAO,TIBETAN,MYANMAR),
	LEFT_TO_RIGHT(GEORGIAN,HANGUL_JAMO,ETHIOPIC,ETHIOPIC_SUPPLEMENT,CHEROKEE,UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS,OGHAM,RUNIC),
	PHILIPPINE(TAGALOG,HANUNOO,BUHID,TAGBANWA),
	TAI(KHMER,MONGOLIAN,UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED,LIMBU,TAI_LE,NEW_TAI_LUE,KHMER_SYMBOLS),
	INDONESIAN(BUGINESE,TAI_THAM,COMBINING_DIACRITICAL_MARKS_EXTENDED,BALINESE,SUNDANESE,BATAK,LEPCHA,OL_CHIKI),
	EXTENSIONS(CYRILLIC_EXTENDED_C,GEORGIAN_EXTENDED,SUNDANESE_SUPPLEMENT,VEDIC_EXTENSIONS,PHONETIC_EXTENSIONS,PHONETIC_EXTENSIONS_SUPPLEMENT,COMBINING_DIACRITICAL_MARKS_SUPPLEMENT,LATIN_EXTENDED_ADDITIONAL,GREEK_EXTENDED),
	SYMBOLS(GENERAL_PUNCTUATION,SUPERSCRIPTS_AND_SUBSCRIPTS,CURRENCY_SYMBOLS,COMBINING_MARKS_FOR_SYMBOLS,LETTERLIKE_SYMBOLS,NUMBER_FORMS,ARROWS,MATHEMATICAL_OPERATORS,MISCELLANEOUS_TECHNICAL,CONTROL_PICTURES,OPTICAL_CHARACTER_RECOGNITION,ENCLOSED_ALPHANUMERICS,BOX_DRAWING,BLOCK_ELEMENTS,GEOMETRIC_SHAPES,MISCELLANEOUS_SYMBOLS,DINGBATS,MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A,SUPPLEMENTAL_ARROWS_A,BRAILLE_PATTERNS,SUPPLEMENTAL_ARROWS_B,MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B,SUPPLEMENTAL_MATHEMATICAL_OPERATORS,MISCELLANEOUS_SYMBOLS_AND_ARROWS),
	SUPPLEMENTS(GLAGOLITIC,LATIN_EXTENDED_C,COPTIC,GEORGIAN_SUPPLEMENT,TIFINAGH,ETHIOPIC_EXTENDED,CYRILLIC_EXTENDED_A,SUPPLEMENTAL_PUNCTUATION),
	CJK(CJK_RADICALS_SUPPLEMENT,KANGXI_RADICALS,null,IDEOGRAPHIC_DESCRIPTION_CHARACTERS,CJK_SYMBOLS_AND_PUNCTUATION,HIRAGANA,KATAKANA,BOPOMOFO,HANGUL_COMPATIBILITY_JAMO,KANBUN,BOPOMOFO_EXTENDED,CJK_STROKES,KATAKANA_PHONETIC_EXTENSIONS,ENCLOSED_CJK_LETTERS_AND_MONTHS,CJK_COMPATIBILITY,CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,YIJING_HEXAGRAM_SYMBOLS,CJK_UNIFIED_IDEOGRAPHS){override fun toString()="CJK"},
	OTHER(YI_SYLLABLES,YI_RADICALS,LISU,VAI,CYRILLIC_EXTENDED_B,BAMUM,MODIFIER_TONE_LETTERS,LATIN_EXTENDED_D),
	INDIC(SYLOTI_NAGRI,COMMON_INDIC_NUMBER_FORMS,PHAGS_PA,SAURASHTRA,DEVANAGARI_EXTENDED,KAYAH_LI,REJANG,HANGUL_JAMO_EXTENDED_A,JAVANESE,MYANMAR_EXTENDED_B,CHAM,MYANMAR_EXTENDED_A,TAI_VIET,MEETEI_MAYEK_EXTENSIONS,ETHIOPIC_EXTENDED_A,LATIN_EXTENDED_E,CHEROKEE_SUPPLEMENT,MEETEI_MAYEK,HANGUL_SYLLABLES,HANGUL_JAMO_EXTENDED_B),
	SURROGATES(HIGH_SURROGATES,HIGH_PRIVATE_USE_SURROGATES,LOW_SURROGATES,PRIVATE_USE_AREA),
	FORMS(CJK_COMPATIBILITY_IDEOGRAPHS,ALPHABETIC_PRESENTATION_FORMS,ARABIC_PRESENTATION_FORMS_A,VARIATION_SELECTORS,VERTICAL_FORMS,COMBINING_HALF_MARKS,CJK_COMPATIBILITY_FORMS,SMALL_FORM_VARIANTS,ARABIC_PRESENTATION_FORMS_B,HALFWIDTH_AND_FULLWIDTH_FORMS,SPECIALS);
	companion object:MutableMap<UnicodeBlock?,CharRange>by mutableMapOf(){
		init{
			for(char in Character.MIN_VALUE..Character.MAX_VALUE){
				val block = char.block
				this[block] = (this[block]?:char..char).first..char
			}
		}
	}
}
