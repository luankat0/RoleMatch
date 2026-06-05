package com.pentagono.rolematch.presentation.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pentagono.rolematch.core.common.RMIcons
import com.pentagono.rolematch.core.theme.DividerLight
import com.pentagono.rolematch.core.theme.PrimaryGreen
import com.pentagono.rolematch.core.theme.TextMuted

// ─── Central de Ajuda ─────────────────────────────────────────────────────────

@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    val faqs = listOf(
        "Como funciona o Rolê Match?" to
                "O Rolê Match é um app que conecta você aos melhores restaurantes da sua cidade. Use o Match para encontrar restaurantes com base nas suas preferências, resgate cupons exclusivos e faça reservas diretamente pelo app.",
        "Como usar um cupom?" to
                "Acesse a aba Cupons, escolha o cupom desejado e toque em 'Gerar QR Code'. Apresente o QR Code ao atendente antes de solicitar a conta.",
        "Como fazer uma reserva?" to
                "Entre na página do restaurante desejado e toque em 'Reservar'. Escolha a data, horário e número de pessoas e confirme.",
        "Como funciona o Match?" to
                "O Match filtra restaurantes com base nas suas preferências: tipo de culinária, faixa de preço, distância, e se aceita pets, crianças ou tem acessibilidade.",
        "Posso cancelar uma reserva?" to
                "Sim. Acesse a aba Perfil > Minhas reservas e toque em 'Cancelar' na reserva desejada. O cancelamento deve ser feito com pelo menos 2 horas de antecedência.",
        "Meus dados estão seguros?" to
                "Sim. Utilizamos criptografia e seguimos todas as diretrizes da LGPD para proteger seus dados pessoais.",
        "Como alterar minha senha?" to
                "Acesse Perfil > Configurações > Privacidade e Segurança > Alterar senha.",
        "O app é gratuito?" to
                "Sim, o Rolê Match é totalmente gratuito para usuários. Os cupons e promoções também são sem custo adicional."
    )

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar")
                    }
                    Text("Central de Ajuda", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Perguntas Frequentes",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Encontre respostas para as dúvidas mais comuns.",
                fontSize = 14.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))

            faqs.forEach { (question, answer) ->
                FaqItem(question = question, answer = answer)
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = DividerLight, modifier = Modifier.padding(vertical = 8.dp))
                Text(text = answer, fontSize = 13.sp, color = TextMuted)
            }
        }
    }
}

// ─── Termos de Uso ────────────────────────────────────────────────────────────

@Composable
fun TermsOfUseScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar")
                    }
                    Text("Termos de Uso", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Termos de Uso",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(text = "Última atualização: maio de 2025", fontSize = 12.sp, color = TextMuted)

            TermsSection(title = "1. Aceitação dos Termos",
                text = "Ao utilizar o aplicativo Rolê Match, você concorda com estes Termos de Uso. Se não concordar, não utilize o aplicativo.")
            TermsSection(title = "2. Uso do Aplicativo",
                text = "O Rolê Match é destinado a maiores de 18 anos. Você é responsável por manter a confidencialidade da sua conta e senha. É proibido usar o app para fins ilegais ou não autorizados.")
            TermsSection(title = "3. Cupons e Promoções",
                text = "Os cupons disponibilizados são de responsabilidade dos restaurantes parceiros. O Rolê Match não garante a disponibilidade ou validade dos cupons. Cada cupom possui regras próprias descritas na sua página.")
            TermsSection(title = "4. Reservas",
                text = "As reservas feitas pelo app são sujeitas à confirmação do restaurante. O não comparecimento sem cancelamento prévio pode resultar em restrições de uso.")
            TermsSection(title = "5. Conteúdo do Usuário",
                text = "Avaliações e comentários devem ser verdadeiros e respeitosos. O Rolê Match reserva o direito de remover conteúdo inapropriado.")
            TermsSection(title = "6. Propriedade Intelectual",
                text = "Todo o conteúdo do Rolê Match, incluindo logo, design e código, é protegido por direitos autorais.")
            TermsSection(title = "7. Limitação de Responsabilidade",
                text = "O Rolê Match não se responsabiliza por problemas ocorridos diretamente nos restaurantes parceiros.")
            TermsSection(title = "8. Alterações nos Termos",
                text = "Podemos atualizar estes termos a qualquer momento. O uso continuado do app após as alterações implica na aceitação dos novos termos.")
            TermsSection(title = "9. Contato",
                text = "Para dúvidas sobre estes termos, entre em contato pelo e-mail: contato@rolematch.com.br")
        }
    }
}

// ─── Política de Privacidade ──────────────────────────────────────────────────

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = RMIcons.ArrowBack, contentDescription = "Voltar")
                    }
                    Text("Política de Privacidade", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Política de Privacidade", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text(text = "Última atualização: maio de 2025", fontSize = 12.sp, color = TextMuted)

            TermsSection(title = "1. Dados Coletados",
                text = "Coletamos: nome, e-mail, telefone, CPF (para usuários), CNPJ (para restaurantes), localização aproximada e dados de uso do aplicativo.")
            TermsSection(title = "2. Como Usamos seus Dados",
                text = "Seus dados são utilizados para: criar e gerenciar sua conta, personalizar recomendações, enviar notificações sobre cupons e promoções, e melhorar a experiência do app.")
            TermsSection(title = "3. Compartilhamento de Dados",
                text = "Não vendemos seus dados. Compartilhamos apenas com restaurantes parceiros o necessário para concluir reservas e resgatar cupons.")
            TermsSection(title = "4. Segurança",
                text = "Utilizamos criptografia e boas práticas de segurança para proteger seus dados. Suas senhas são armazenadas de forma criptografada.")
            TermsSection(title = "5. Seus Direitos (LGPD)",
                text = "Você tem o direito de: acessar seus dados, corrigir informações incorretas, solicitar a exclusão da sua conta e revogar consentimentos a qualquer momento.")
            TermsSection(title = "6. Cookies e Rastreamento",
                text = "Utilizamos tecnologias de análise para entender como o app é usado e melhorá-lo continuamente.")
            TermsSection(title = "7. Retenção de Dados",
                text = "Mantemos seus dados enquanto sua conta estiver ativa. Após a exclusão da conta, os dados são removidos em até 30 dias.")
            TermsSection(title = "8. Contato",
                text = "Para exercer seus direitos ou tirar dúvidas sobre privacidade: contato@rolematch.com.br")
        }
    }
}

@Composable
private fun TermsSection(title: String, text: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryGreen)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}