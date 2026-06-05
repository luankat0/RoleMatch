package com.pentagono.rolematch.presentation.screens.restaurant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pentagono.rolematch.core.common.ImageUploadHelper
import com.pentagono.rolematch.core.theme.*
import com.pentagono.rolematch.presentation.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── ViewModel ────────────────────────────────────────────────────────────────

data class RestaurantEditState(
    val name: String = "",
    val description: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val instagram: String = "",
    val website: String = "",
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val photos: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String = "",
    val saveSuccess: Boolean = false,
    val restaurantId: String = ""
)

class RestaurantEditViewModel : ViewModel() {

    private val _state = MutableStateFlow(RestaurantEditState())
    val state: StateFlow<RestaurantEditState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val query = firestore.collection("restaurants")
                    .whereEqualTo("ownerId", uid).limit(1).get().await()
                val doc = query.documents.firstOrNull() ?: return@launch
                val address = (doc.get("address") as? Map<*, *>)
                val photos = (doc.get("photos") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                _state.value = _state.value.copy(
                    restaurantId = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    phone = doc.getString("phone") ?: "",
                    whatsapp = doc.getString("whatsapp") ?: "",
                    instagram = doc.getString("instagram") ?: "",
                    website = doc.getString("website") ?: "",
                    cep = address?.get("cep") as? String ?: "",
                    street = address?.get("street") as? String ?: "",
                    number = address?.get("number") as? String ?: "",
                    neighborhood = address?.get("neighborhood") as? String ?: "",
                    city = address?.get("city") as? String ?: "",
                    photos = photos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Erro ao carregar dados")
            }
        }
    }

    fun uploadPhoto(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            update { copy(isUploadingPhoto = true, error = "") }
            val result = ImageUploadHelper.uploadRestaurantPhoto(context, uri, _state.value.restaurantId)
            result.fold(
                onSuccess = { url ->
                    val newPhotos = _state.value.photos + url
                    firestore.collection("restaurants")
                        .document(_state.value.restaurantId)
                        .update("photos", newPhotos)
                        .await()
                    update { copy(photos = newPhotos, isUploadingPhoto = false) }
                },
                onFailure = { e ->
                    update { copy(isUploadingPhoto = false, error = "Erro ao enviar foto: ${e.message}") }
                }
            )
        }
    }

    fun deletePhoto(url: String) {
        viewModelScope.launch {
            update { copy(isUploadingPhoto = true) }
            ImageUploadHelper.deleteRestaurantPhoto(url)
            val newPhotos = _state.value.photos - url
            firestore.collection("restaurants")
                .document(_state.value.restaurantId)
                .update("photos", newPhotos)
                .await()
            update { copy(photos = newPhotos, isUploadingPhoto = false) }
        }
    }

    fun onNameChange(v: String) = update { copy(name = v) }
    fun onDescriptionChange(v: String) = update { copy(description = v) }
    fun onPhoneChange(v: String) = update { copy(phone = v) }
    fun onWhatsappChange(v: String) = update { copy(whatsapp = v) }
    fun onInstagramChange(v: String) = update { copy(instagram = v) }
    fun onWebsiteChange(v: String) = update { copy(website = v) }
    fun onCepChange(v: String) = update { copy(cep = v) }
    fun onStreetChange(v: String) = update { copy(street = v) }
    fun onNumberChange(v: String) = update { copy(number = v) }
    fun onNeighborhoodChange(v: String) = update { copy(neighborhood = v) }
    fun onCityChange(v: String) = update { copy(city = v) }
    fun onSaveSuccessHandled() = update { copy(saveSuccess = false) }

    fun save() {
        val s = _state.value
        if (s.name.trim().length < 2) {
            update { copy(error = "Nome do restaurante é obrigatório") }
            return
        }
        viewModelScope.launch {
            update { copy(isSaving = true, error = "") }
            try {
                val data = mapOf(
                    "name" to s.name.trim(),
                    "description" to s.description.trim(),
                    "phone" to s.phone.trim(),
                    "whatsapp" to s.whatsapp.trim(),
                    "instagram" to s.instagram.trim(),
                    "website" to s.website.trim(),
                    "address" to mapOf(
                        "cep" to s.cep.trim(),
                        "street" to s.street.trim(),
                        "number" to s.number.trim(),
                        "neighborhood" to s.neighborhood.trim(),
                        "city" to s.city.trim()
                    )
                )
                firestore.collection("restaurants").document(s.restaurantId).update(data).await()
                update { copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                update { copy(isSaving = false, error = e.message ?: "Erro ao salvar") }
            }
        }
    }

    private fun update(block: RestaurantEditState.() -> RestaurantEditState) {
        _state.value = _state.value.block()
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RestaurantEditScreen(
    onBack: () -> Unit,
    viewModel: RestaurantEditViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(context, it) }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.onSaveSuccessHandled()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                        Text("Informações do Restaurante", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (state.error.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(state.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    PrimaryButton(
                        text = "Salvar alterações",
                        onClick = viewModel::save,
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── FOTOS ──────────────────────────────────────────────────
            EditCard(title = "Fotos do Restaurante") {
                Text(
                    "Adicione fotos do seu restaurante para atrair mais clientes.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Botão adicionar foto
                    item {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, PrimaryGreen, RoundedCornerShape(12.dp))
                                .clickable(enabled = !state.isUploadingPhoto) {
                                    photoPickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isUploadingPhoto) {
                                CircularProgressIndicator(
                                    color = PrimaryGreen,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text("Adicionar", fontSize = 11.sp, color = PrimaryGreen)
                                }
                            }
                        }
                    }

                    // Fotos existentes
                    items(state.photos) { photoUrl ->
                        Box(
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Botão remover
                            IconButton(
                                onClick = { viewModel.deletePhoto(photoUrl) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                if (state.photos.isEmpty() && !state.isUploadingPhoto) {
                    Spacer(Modifier.height(8.dp))
                    Text("Nenhuma foto adicionada ainda.", fontSize = 12.sp, color = TextLight)
                }
            }

            // ── INFORMAÇÕES BÁSICAS ────────────────────────────────────
            EditCard(title = "Informações Básicas") {
                RoleMatchTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome do restaurante",
                    leadingIcon = Icons.Default.Storefront,
                    placeholder = "Ex: Rolê Match Bistrô"
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descrição") },
                    placeholder = { Text("Conte sobre seu restaurante...", color = TextLight) },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── CONTATO ────────────────────────────────────────────────
            EditCard(title = "Contato e Redes Sociais") {
                RoleMatchTextField(value = state.phone, onValueChange = viewModel::onPhoneChange, label = "Telefone", leadingIcon = Icons.Default.Call, placeholder = "(11) 98765-4321")
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(value = state.whatsapp, onValueChange = viewModel::onWhatsappChange, label = "WhatsApp (opcional)", leadingIcon = Icons.Default.Call, placeholder = "(11) 98765-4321")
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(value = state.instagram, onValueChange = viewModel::onInstagramChange, label = "Instagram (opcional)", leadingIcon = Icons.Default.Link, placeholder = "@seurestaurante")
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(value = state.website, onValueChange = viewModel::onWebsiteChange, label = "Website (opcional)", leadingIcon = Icons.Default.Language, placeholder = "www.seurestaurante.com.br")
            }

            // ── ENDEREÇO ───────────────────────────────────────────────
            EditCard(title = "Endereço") {
                RoleMatchTextField(value = state.cep, onValueChange = viewModel::onCepChange, label = "CEP", placeholder = "00000-000")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoleMatchTextField(value = state.street, onValueChange = viewModel::onStreetChange, label = "Logradouro", placeholder = "Av. Principal", modifier = Modifier.weight(2f))
                    RoleMatchTextField(value = state.number, onValueChange = viewModel::onNumberChange, label = "Número", placeholder = "123", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(value = state.neighborhood, onValueChange = viewModel::onNeighborhoodChange, label = "Bairro", placeholder = "Centro")
                Spacer(Modifier.height(12.dp))
                RoleMatchTextField(value = state.city, onValueChange = viewModel::onCityChange, label = "Cidade", placeholder = "São Paulo")
            }
        }
    }
}

@Composable
private fun EditCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}