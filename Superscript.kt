val String.superscript get() = this.replace(Regex(".")){it->""+try{Superscript.valueOf(it.value)}catch(_:IllegalArgumentException){Superscript.valueOf(it.value.lowercase())}}
val Char.superscript get() = toString().superscript

enum class Superscript(private val sup:String){
	`0`("⁰"),`1`("¹"),`2`("²"),`3`("³"),`4`("⁴"),`5`("⁵"),`6`("⁶"),`7`("⁷"),`8`("⁸"),`9`("⁹"),
	A("ᴬ"),B("ᴮ"),C("ꟲ"),D("ᴰ"),E("ᴱ"),F("ꟳ"),G("ᴳ"),H("ᴴ"),I("ᴵ"),J("ᴶ"),K("ᴷ"),L("ᴸ"),M("ᴹ"),
	N("ᴺ"),O("ᴼ"),P("ᴾ"),Q("ꟴ"),R("ᴿ"),       T("ᵀ"),U("ᵁ"),V("ⱽ"),W("ᵂ"),
	a("ᵃ"),b("ᵇ"),c("ᶜ"),d("ᵈ"),e("ᵉ"),f("ᶠ"),g("ᵍ"),h("ʰ"),i("ⁱ"),j("ʲ"),k("ᵏ"),l("ˡ"),m("ᵐ"),
	n("ⁿ"),o("ᵒ"),p("ᵖ"),q("𐞥"),r("ʳ"),s("ˢ"),t("ᵗ"),u("ᵘ"),v("ᵛ"),w("ʷ"),x("ˣ"),y("ʸ"),z("ᶻ"),
	`+`("⁺"),`-`("⁻"),`=`("⁼"),`(`("⁽"),`)`("⁾");
	override fun toString()=this.sup
}