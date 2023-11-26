package io.github.simonschiller.prefiller.internal.util

import com.ibm.icu.text.Collator
import com.ibm.icu.util.ULocale
import org.sqlite.Collation

internal class SqliteIcuCollation(
    locale: ULocale = ULocale.ROOT,
) : Collation() {
    private val collator: Collator = Collator.getInstance(locale)
    override fun xCompare(str1: String, str2: String): Int = collator.compare(str1, str2)
}
