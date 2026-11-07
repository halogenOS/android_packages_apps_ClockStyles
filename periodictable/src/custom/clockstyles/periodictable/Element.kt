// SPDX-FileCopyrightText: The halogenOS Project
// SPDX-License-Identifier: Apache-2.0

package custom.clockstyles.periodictable

import android.annotation.StringRes

/** The atomic weight shown on an element tile. */
sealed interface AtomicWeight {
    /** The abridged standard atomic weight. */
    data class Standard(val value: String) : AtomicWeight

    /** The mass number of the longest-lived isotope, for an element without a standard weight. */
    data class LongestLivedIsotope(val massNumber: Int) : AtomicWeight
}

/**
 * The elements a clock shows, one for each hour and minute from 1 to 59, with the atomic number
 * [z], the symbol, the atomic weight and the name.
 *
 * Symbols and abridged standard atomic weights as the IUPAC Commission on Isotopic Abundances and
 * Atomic Weights lists them in "Abridged Standard Atomic Weights 2024",
 * https://www.ciaaw.org/abridged-atomic-weights.htm, checked on 2026-11-07. Technetium does not
 * have a standard atomic weight there. Its longest-lived isotope is 97Tc (4.21 million years).
 */
enum class Element(
    val z: Int,
    val symbol: String,
    val weight: AtomicWeight,
    @get:StringRes val nameRes: Int,
) {
    HYDROGEN(1, "H", AtomicWeight.Standard("1.0080"), R.string.element_hydrogen),
    HELIUM(2, "He", AtomicWeight.Standard("4.0026"), R.string.element_helium),
    LITHIUM(3, "Li", AtomicWeight.Standard("6.94"), R.string.element_lithium),
    BERYLLIUM(4, "Be", AtomicWeight.Standard("9.0122"), R.string.element_beryllium),
    BORON(5, "B", AtomicWeight.Standard("10.81"), R.string.element_boron),
    CARBON(6, "C", AtomicWeight.Standard("12.011"), R.string.element_carbon),
    NITROGEN(7, "N", AtomicWeight.Standard("14.007"), R.string.element_nitrogen),
    OXYGEN(8, "O", AtomicWeight.Standard("15.999"), R.string.element_oxygen),
    FLUORINE(9, "F", AtomicWeight.Standard("18.998"), R.string.element_fluorine),
    NEON(10, "Ne", AtomicWeight.Standard("20.180"), R.string.element_neon),
    SODIUM(11, "Na", AtomicWeight.Standard("22.990"), R.string.element_sodium),
    MAGNESIUM(12, "Mg", AtomicWeight.Standard("24.305"), R.string.element_magnesium),
    ALUMINIUM(13, "Al", AtomicWeight.Standard("26.982"), R.string.element_aluminium),
    SILICON(14, "Si", AtomicWeight.Standard("28.085"), R.string.element_silicon),
    PHOSPHORUS(15, "P", AtomicWeight.Standard("30.974"), R.string.element_phosphorus),
    SULFUR(16, "S", AtomicWeight.Standard("32.06"), R.string.element_sulfur),
    CHLORINE(17, "Cl", AtomicWeight.Standard("35.45"), R.string.element_chlorine),
    ARGON(18, "Ar", AtomicWeight.Standard("39.95"), R.string.element_argon),
    POTASSIUM(19, "K", AtomicWeight.Standard("39.098"), R.string.element_potassium),
    CALCIUM(20, "Ca", AtomicWeight.Standard("40.078"), R.string.element_calcium),
    SCANDIUM(21, "Sc", AtomicWeight.Standard("44.956"), R.string.element_scandium),
    TITANIUM(22, "Ti", AtomicWeight.Standard("47.867"), R.string.element_titanium),
    VANADIUM(23, "V", AtomicWeight.Standard("50.942"), R.string.element_vanadium),
    CHROMIUM(24, "Cr", AtomicWeight.Standard("51.996"), R.string.element_chromium),
    MANGANESE(25, "Mn", AtomicWeight.Standard("54.938"), R.string.element_manganese),
    IRON(26, "Fe", AtomicWeight.Standard("55.845"), R.string.element_iron),
    COBALT(27, "Co", AtomicWeight.Standard("58.933"), R.string.element_cobalt),
    NICKEL(28, "Ni", AtomicWeight.Standard("58.693"), R.string.element_nickel),
    COPPER(29, "Cu", AtomicWeight.Standard("63.546"), R.string.element_copper),
    ZINC(30, "Zn", AtomicWeight.Standard("65.38"), R.string.element_zinc),
    GALLIUM(31, "Ga", AtomicWeight.Standard("69.723"), R.string.element_gallium),
    GERMANIUM(32, "Ge", AtomicWeight.Standard("72.630"), R.string.element_germanium),
    ARSENIC(33, "As", AtomicWeight.Standard("74.922"), R.string.element_arsenic),
    SELENIUM(34, "Se", AtomicWeight.Standard("78.971"), R.string.element_selenium),
    BROMINE(35, "Br", AtomicWeight.Standard("79.904"), R.string.element_bromine),
    KRYPTON(36, "Kr", AtomicWeight.Standard("83.798"), R.string.element_krypton),
    RUBIDIUM(37, "Rb", AtomicWeight.Standard("85.468"), R.string.element_rubidium),
    STRONTIUM(38, "Sr", AtomicWeight.Standard("87.62"), R.string.element_strontium),
    YTTRIUM(39, "Y", AtomicWeight.Standard("88.906"), R.string.element_yttrium),
    ZIRCONIUM(40, "Zr", AtomicWeight.Standard("91.222"), R.string.element_zirconium),
    NIOBIUM(41, "Nb", AtomicWeight.Standard("92.906"), R.string.element_niobium),
    MOLYBDENUM(42, "Mo", AtomicWeight.Standard("95.95"), R.string.element_molybdenum),
    TECHNETIUM(43, "Tc", AtomicWeight.LongestLivedIsotope(97), R.string.element_technetium),
    RUTHENIUM(44, "Ru", AtomicWeight.Standard("101.07"), R.string.element_ruthenium),
    RHODIUM(45, "Rh", AtomicWeight.Standard("102.91"), R.string.element_rhodium),
    PALLADIUM(46, "Pd", AtomicWeight.Standard("106.42"), R.string.element_palladium),
    SILVER(47, "Ag", AtomicWeight.Standard("107.87"), R.string.element_silver),
    CADMIUM(48, "Cd", AtomicWeight.Standard("112.41"), R.string.element_cadmium),
    INDIUM(49, "In", AtomicWeight.Standard("114.82"), R.string.element_indium),
    TIN(50, "Sn", AtomicWeight.Standard("118.71"), R.string.element_tin),
    ANTIMONY(51, "Sb", AtomicWeight.Standard("121.76"), R.string.element_antimony),
    TELLURIUM(52, "Te", AtomicWeight.Standard("127.60"), R.string.element_tellurium),
    IODINE(53, "I", AtomicWeight.Standard("126.90"), R.string.element_iodine),
    XENON(54, "Xe", AtomicWeight.Standard("131.29"), R.string.element_xenon),
    CAESIUM(55, "Cs", AtomicWeight.Standard("132.91"), R.string.element_caesium),
    BARIUM(56, "Ba", AtomicWeight.Standard("137.33"), R.string.element_barium),
    LANTHANUM(57, "La", AtomicWeight.Standard("138.91"), R.string.element_lanthanum),
    CERIUM(58, "Ce", AtomicWeight.Standard("140.12"), R.string.element_cerium),
    PRASEODYMIUM(59, "Pr", AtomicWeight.Standard("140.91"), R.string.element_praseodymium);

    companion object {
        private val byNumber = entries.associateBy(Element::z)

        /** The element with the atomic number [z], or null for a number without one, such as 0. */
        fun numbered(z: Int): Element? = byNumber[z]
    }
}
