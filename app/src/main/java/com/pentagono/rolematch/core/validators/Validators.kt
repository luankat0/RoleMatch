package com.pentagono.rolematch.core.validators

object CpfValidator {
    fun isValid(cpf: String): Boolean {
        val digits = cpf.replace(Regex("[^0-9]"), "")
        if (digits.length != 11) return false
        if (digits.all { it == digits[0] }) return false

        fun calcDigit(slice: String, weights: IntArray): Int {
            val sum = slice.mapIndexed { i, c -> c.digitToInt() * weights[i] }.sum()
            val rem = sum % 11
            return if (rem < 2) 0 else 11 - rem
        }

        val w1 = intArrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2)
        val w2 = intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2)

        val d1 = calcDigit(digits.substring(0, 9), w1)
        val d2 = calcDigit(digits.substring(0, 10), w2)

        return digits[9].digitToInt() == d1 && digits[10].digitToInt() == d2
    }

    fun format(raw: String): String {
        val d = raw.replace(Regex("[^0-9]"), "").take(11)
        return buildString {
            d.forEachIndexed { i, c ->
                if (i == 3 || i == 6) append('.')
                if (i == 9) append('-')
                append(c)
            }
        }
    }

    fun strip(cpf: String) = cpf.replace(Regex("[^0-9]"), "")
}

object CnpjValidator {

    // CNPJ numérico clássico:    12.345.678/0001-95
    // CNPJ alfanumérico (2026+): 12.ABC.345/0001-35
    // Regra: 12 chars alfanuméricos + 2 dígitos verificadores numéricos
    // Formato de máscara: XX.XXX.XXX/XXXX-XX (16 chars com máscara)

    private val WEIGHTS_D1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    private val WEIGHTS_D2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

    // Converte char alfanumérico para valor numérico conforme IN RFB 2.229/2024
    // Dígitos 0-9 → valor próprio; Letras A-Z → 17-42 (A=17, B=18, ...)
    private fun charValue(c: Char): Int {
        return if (c.isDigit()) c.digitToInt()
        else c.uppercaseChar().code - 'A'.code + 17
    }

    private fun calcDigit(chars: String, weights: IntArray): Int {
        val sum = chars.mapIndexed { i, c -> charValue(c) * weights[i] }.sum()
        val rem = sum % 11
        return if (rem < 2) 0 else 11 - rem
    }

    fun isValid(cnpj: String): Boolean {
        // Remove máscara: pontos, barra, hífen
        val raw = cnpj.replace(Regex("[.\\-/]"), "").uppercase()

        if (raw.length != 14) return false

        // Verifica se não são todos iguais (sequência inválida)
        if (raw.all { it == raw[0] }) return false

        // Os 2 últimos devem ser dígitos (verificadores sempre numéricos)
        if (!raw[12].isDigit() || !raw[13].isDigit()) return false

        // Valida caracteres: posições 0-11 alfanuméricos, 12-13 numéricos
        if (!raw.substring(0, 12).all { it.isLetterOrDigit() }) return false

        val d1 = calcDigit(raw.substring(0, 12), WEIGHTS_D1)
        val d2 = calcDigit(raw.substring(0, 13), WEIGHTS_D2)

        return raw[12].digitToInt() == d1 && raw[13].digitToInt() == d2
    }

    // Formata conforme o usuário digita: XX.XXX.XXX/XXXX-XX
    // Aceita letras e números nos primeiros 12 chars
    fun format(raw: String): String {
        // Remove máscara e converte letras para maiúsculo
        val clean = raw.replace(Regex("[.\\-/]"), "").uppercase()
            .filter { it.isLetterOrDigit() }
            .take(14)

        return buildString {
            clean.forEachIndexed { i, c ->
                if (i == 2 || i == 5) append('.')
                if (i == 8) append('/')
                if (i == 12) append('-')
                append(c)
            }
        }
    }

    // Remove máscara para armazenar no banco
    fun strip(cnpj: String) = cnpj.replace(Regex("[.\\-/]"), "").uppercase()

    // Detecta se é CNPJ alfanumérico (contém letras nos 12 primeiros chars)
    fun isAlphanumeric(cnpj: String): Boolean {
        val raw = strip(cnpj)
        return raw.length == 14 && raw.substring(0, 12).any { it.isLetter() }
    }
}

object EmailValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    fun isValid(email: String) = emailRegex.matches(email.trim())
}

object PhoneValidator {
    fun isValid(phone: String): Boolean {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return digits.length in 10..11
    }

    fun format(raw: String): String {
        val d = raw.replace(Regex("[^0-9]"), "").take(11)
        return buildString {
            if (d.isEmpty()) return@buildString
            append('(')
            d.forEachIndexed { i, c ->
                append(c)
                when {
                    i == 1 -> append(") ")
                    i == 6 && d.length == 11 -> append('-')
                    i == 5 && d.length == 10 -> append('-')
                }
            }
        }
    }
}

object PasswordValidator {
    data class PasswordStrength(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validate(password: String): PasswordStrength {
        val errors = mutableListOf<String>()
        if (password.length < 8)
            errors += "Mínimo 8 caracteres"
        if (!password.any { it.isUpperCase() })
            errors += "1 letra maiúscula"
        if (!password.any { it.isLowerCase() })
            errors += "1 letra minúscula"
        if (!password.any { it.isDigit() })
            errors += "1 número"
        if (!password.any { !it.isLetterOrDigit() })
            errors += "1 caractere especial"
        return PasswordStrength(isValid = errors.isEmpty(), errors = errors)
    }
}