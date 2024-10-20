import java.lang.Character.UnicodeScript.*
import java.lang.Character.UnicodeScript
import java.util.EnumMap

val Char.script: UnicodeScript get() = of(code)
val UnicodeScript.family: UnicodeScriptFamily get() = UnicodeScriptFamily.values().first{this in it}
val UnicodeScript.code: String get() = UnicodeScriptFamily[this]!!
val UnicodeScript.label: String get() = "'${title.replace("Canadian","🇨🇦").replace(' ',' ')} ${code.superscript}'"
val UnicodeScriptFamily.label: String get() = "'${title.replace(' ','-')} $code'"
val Enum<*>?.label: String get() = title.let{if(' ' in it)"'$it'" else it}

/**https://scriptsource.org/cms/scripts/page.php?item_id=script_overview_full&sort_script_overview=script_family*/
enum class UnicodeScriptFamily(val code:String,vararg scripts:UnicodeScript):Set<UnicodeScript>by setOf(*scripts){
	SPECIAL("🇺🇳",COMMON,INHERITED,UNKNOWN),//https://wikipedia.org/wiki/Script_(Unicode)#Special_script_property_values
	EUROPEAN("🇪🇺",LATIN,GREEK,CYRILLIC,ARMENIAN,GEORGIAN,OGHAM,RUNIC,COPTIC,GLAGOLITIC),
	AFRICAN("🇪🇹",ETHIOPIC,TIFINAGH,NKO,VAI,BAMUM),
	MIDDLE_EASTERN("🇸🇦",HEBREW,ARABIC,SYRIAC,SAMARITAN,MANDAIC),
	NORTH_INDIC("🇮🇳",DEVANAGARI,BENGALI,GURMUKHI,GUJARATI,ORIYA,LIMBU,SYLOTI_NAGRI,MEETEI_MAYEK),//https://wikipedia.org/wiki/Brahmic_scripts#Northern_Brahmic
	SOUTH_INDIC("🇱🇰",THAANA,TAMIL,TELUGU,KANNADA,MALAYALAM,SINHALA,OL_CHIKI,SAURASHTRA),//https://wikipedia.org/wiki/Brahmic_scripts#Southern_Brahmic
	CENTRAL_ASIAN("🇲🇳",TIBETAN,MONGOLIAN,PHAGS_PA,LEPCHA),
	EAST_ASIAN("🇯🇵",HANGUL,HIRAGANA,KATAKANA),
	CHINESE("🇨🇳",BOPOMOFO,HAN,YI,LISU),
	TAI("🇹🇭",THAI,TAI_LE,NEW_TAI_LUE,TAI_THAM,TAI_VIET),
	SOUTHEAST_ASIAN("🇲🇲",LAO,MYANMAR,KHMER,KAYAH_LI,CHAM),
	INDONESIAN("🇮🇩",BUGINESE,BALINESE,SUNDANESE,BATAK,REJANG,JAVANESE),
	PHILIPPINE("🇵🇭",TAGALOG,HANUNOO,BUHID,TAGBANWA),
	OTHER("🇺🇸",BRAILLE,CHEROKEE,CANADIAN_ABORIGINAL);
	companion object:EnumMap<UnicodeScript,String>(mapOf(UNKNOWN to "")){
		init{
			for(a in 'A'..'Z')for(b in 'a'..'z')for(c in 'a'..'z')for(d in 'a'..'z')try{
				val name = "$a$b$c$d"
				val script = forName(name)
				put(script, name)
			}catch(_:IllegalArgumentException){}
		}
	}
}