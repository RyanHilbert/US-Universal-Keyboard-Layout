# US Universal Keyboard Layout
A native keyboard layout for Windows that enables simple typing of complex characters.
## Installation and Use
Simply download and run the installer from the Releases page. You can then select the United States-Universal layout from the Windows language and keyboard options menu. Optionally, you can also download and run CharMap.jar using at least Java 7 for a detailed, searchable list of supported characters.
## How it Works
The Node.kt Kotlin source file is used to programmatically generate the hundreds of different Compose key sequences and convert them into a format understood by the software KbdEdit. The rest of the layout features and the installer are created directly with KbdEdit itself.
## Applications
The layout supports a wide range of use cases. Its various new modifier keys can be held to access an array of supplementary characters, while the Compose key (Right-Ctrl) can be pressed as the beginning of a sequence to combine standard characters into more interesting ones.
### International
Improving upon the US International layout, the Compose (Right-Ctrl) and AltGR (Right-Alt) keys can be used to directly access a multitude of international characters, such as ligatures, accented vowels, and foreign currency symbols.
### Financial
The Compose key enables simple, intuitive input of most of the world's currency symbols, via sequences involving the dollar sign ($) key.
### Mathematics
The Compose key also allows input of various mathematical symbols and constants; integrals, inequalities, and other operators are all supported. Additionally, the AltGR (Right-Alt) key allows access to Greek characters, and CapsLock enables super/subscripts.
### Science
2Mn²⁺ + 5S₂O₈²⁻ + 8H₂O → 2MnO₄ + 10SO₄²⁻ + 16H⁺
### Social
☺ ❀ ☃ ⚘ ❤
### Normal Stuff
Even with so many features, this layout tries to stay out of your way during everyday typing. The only keys with different behaviors than in the standard US layout are:
- CapsLock now enables super/subscripts instead of capitals
- The Right-Alt key can now be used as a modifier key to type Greek characters
- The Right-Windows key can now be used as a modifier key to type bold characters
## Example Composition Sequences
⎄ = Compose Key (Right-Ctrl)
- ⎄<3 → ❤
- ⎄<- → ≤
- ⎄/= → ≠
- ⎄3S → ∭
- ⎄"-U → Ǖ
- ⎄knight → ♘
- ⎄KNIGHT → ♞
