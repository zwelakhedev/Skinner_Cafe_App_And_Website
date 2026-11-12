package za.co.skinnercafe.app

import androidx.compose.material3.RadioButton

import androidx.compose.material3.Checkbox

import android.os.Bundle
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.foundation.clickable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import za.co.skinnercafe.app.data.api.MenuCategory
import za.co.skinnercafe.app.data.api.MenuProduct
import za.co.skinnercafe.app.data.api.MenuRepository
import za.co.skinnercafe.app.ui.theme.SkinnerCafeTheme
import za.co.skinnercafe.app.navigation.SkinnerCafeNavigation
import za.co.skinnercafe.app.data.api.CustomerDto
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.ui.platform.LocalContext
import za.co.skinnercafe.app.ui.auth.AuthUiState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import za.co.skinnercafe.app.ui.settings.AppPreferences
import za.co.skinnercafe.app.ui.theme.LocalCafeColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appPreferences = remember {
                AppPreferences(applicationContext)
            }

            val isDarkTheme by appPreferences.isDarkTheme.collectAsState(
                initial = false
            )

            SkinnerCafeTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                SkinnerCafeNavigation(
                    homeContent = {
                            customer,
                            onLogout,
                            onUploadProfilePicture,
                            onRemoveProfilePicture,
                            authState,
                            onOpenSettings ->
                        SkinnerCafeHome(
                            customer = customer,
                            onLogout = onLogout,
                            onUploadProfilePicture = onUploadProfilePicture,
                            onRemoveProfilePicture = onRemoveProfilePicture,
                            authState = authState,
                            onOpenSettings = onOpenSettings
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SkinnerCafeHome(
    customer: CustomerDto? = null,
    onLogout: () -> Unit = {},
    onUploadProfilePicture: (MultipartBody.Part) -> Unit = {},
    onRemoveProfilePicture: () -> Unit = {},
    authState: AuthUiState = AuthUiState(),
    onOpenSettings: () -> Unit = {}
) {
    if (authState.isLoading) {
        CircularProgressIndicator()
    }

    authState.successMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    authState.errorMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
    }
    val customerInitials = customer?.let {
        "${it.firstName.firstOrNull() ?: ""}${it.lastName.firstOrNull() ?: ""}"
            .uppercase()
    }?.ifBlank { "SC" } ?: "SC"
    var categories by remember { mutableStateOf<List<MenuCategory>>(emptyList()) }
    var products by remember { mutableStateOf<List<MenuProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("Coffee") }
    var selectedTab by remember { mutableStateOf("Home") }
    var selectedProduct by remember {
        mutableStateOf<MenuProduct?>(null)
    }
    var cartItems by remember {
        mutableStateOf<List<CartItem>>(emptyList())
    }
    var isCheckoutScreen by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val imagePart = createProfilePicturePart(
                context = context,
                uri = uri
            )

            if (imagePart != null) {
                onUploadProfilePicture(imagePart)
            } else {
                Toast.makeText(
                    context,
                    "Unable to read image or image exceeds 5 MB.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    var isAccountMenuExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        try {
            val repository = MenuRepository()
            categories = repository.getCategories()
            products = repository.getProducts()
            loadError = null
        } catch (exception: Exception) {
        Log.e("SkinnerCafeMenu", "Failed to load menu", exception)
        loadError = "Unable to load the menu. Please try again."
        } finally {
            isLoading = false
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            CafeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == "Profile") {
                        onOpenSettings()
                    } else {
                        selectedTab = tab
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedProduct != null) {
                ProductDetailsScreen(
                    product = selectedProduct!!,
                    onBack = {
                        selectedProduct = null
                    },
                    onAddToCart = { item ->
                        val existingIndex = cartItems.indexOfFirst { existing ->
                            existing.product.productId == item.product.productId &&
                                    existing.variant?.variantId == item.variant?.variantId &&
                                    existing.addOns.map { it.addOnId }.toSet() ==
                                    item.addOns.map { it.addOnId }.toSet()
                        }

                        cartItems = if (existingIndex >= 0) {
                            cartItems.mapIndexed { index, existing ->
                                if (index == existingIndex) {
                                    existing.copy(
                                        quantity = existing.quantity + item.quantity
                                    )
                                } else {
                                    existing
                                }
                            }
                        } else {
                            cartItems + item
                        }

                        selectedProduct = null
                        selectedTab = "Cart"
                    }
                )
            } else if (selectedTab == "Menu") {
                SkinnerCafeMenuScreen(
                    categories = categories,
                    products = products,
                    isLoading = isLoading,
                    loadError = loadError,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onViewItem = { product -> selectedProduct = product }
                )
            } else if (selectedTab == "Cart" && isCheckoutScreen) {
                CheckoutScreen(
                    customer = customer,
                    cartItems = cartItems,
                    onBack = {
                        isCheckoutScreen = false
                    },
                    onOrderPlaced = {
                        cartItems = emptyList()
                    }
                )
            } else if (selectedTab == "Cart") {
                CartScreen(
                    cartItems = cartItems,
                    onQuantityChange = { index, quantity ->
                        cartItems = cartItems.toMutableList().also {
                            it[index] = it[index].copy(quantity = quantity)
                        }
                    },
                    onRemoveItem = { index ->
                        cartItems = cartItems.toMutableList().also {
                            it.removeAt(index)
                        }
                    },
                    onCheckout = {
                        if (cartItems.isNotEmpty()) {
                            isCheckoutScreen = true
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
            Spacer(Modifier.height(18.dp))

            // Brand header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Image(
                        painter = painterResource(R.drawable.skinner_cafe_logo),
                        contentDescription = "Skinner Cafe logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(156.dp)
                            .height(58.dp)
                    )

                    Text(
                        text = "YOUR DAILY COFFEE RITUAL",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.7.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable {
                                isAccountMenuExpanded = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!customer?.profilePictureUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = customer?.profilePictureUrl,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(46.dp)
                                )
                            } else {
                                Text(
                                    text = customerInitials,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = isAccountMenuExpanded,
                        onDismissRequest = { isAccountMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Upload Profile Picture") },
                            enabled = !authState.isLoading,
                            onClick = {
                                isAccountMenuExpanded = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )

                        if (!customer?.profilePictureUrl.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("Remove Profile Picture") },
                                enabled = !authState.isLoading,
                                onClick = {
                                    isAccountMenuExpanded = false
                                    onRemoveProfilePicture()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("My Orders") },
                            onClick = {
                                isAccountMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                isAccountMenuExpanded = false
                                onLogout()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Editorial-style hero image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(252.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.caffe_latte),
                    contentDescription = "Freshly prepared cafe latte",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.76f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MADE FOR YOUR MOMENT",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Good coffee.\nBetter moments.",
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 31.sp,
                        lineHeight = 37.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Your favourite cup is waiting.",
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { selectedTab = "Menu" },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            text = "DISCOVER THE MENU",
                            fontSize = 11.sp,
                            letterSpacing = 0.7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // Categories
            SectionHeading(
                title = "Explore the menu",
                subtitle = "A little something for every mood"
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                categories.forEach { category ->
                    CategoryChip(
                        label = category.categoryName,
                        selected = category.categoryName.equals(
                            selectedCategory,
                            ignoreCase = true
                        ),
                        onClick = {
                            selectedCategory = category.categoryName
                        }
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // Featured products
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Customer favourites",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Worth coming back for.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "OUR PICKS",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                val featuredProducts = products
                    .filter {
                        it.category.equals(
                            selectedCategory,
                            ignoreCase = true
                        )
                    }
                    .take(2)

                featuredProducts.forEach { product ->
                    ProductCard(
                        modifier = Modifier.weight(1f),
                        imageRes = productImageResource(product.productName),
                        name = product.productName,
                        description = product.description
                            ?: "Freshly prepared at Skinner Cafe",
                        price = "From R ${
                            String.format(
                                java.util.Locale.US,
                                "%.2f",
                                product.variants.minOfOrNull { it.price } ?: 0.0
                            )
                        }",
                        onViewItem = {
                            selectedProduct = product
                        }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Brand statement
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "A moment to slow down.",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "From your first morning coffee to an afternoon treat, " +
                            "make a little room for something delicious.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 21.sp
                )
            }

                    Spacer(Modifier.height(26.dp))
                }
            }
        }
    }
}

private fun createProfilePicturePart(
    context: Context,
    uri: Uri
): MultipartBody.Part? {
    val contentResolver = context.contentResolver

    val mimeType = contentResolver.getType(uri) ?: return null

    // Check the selected image size before reading it into memory.
    val cursor = contentResolver.query(
        uri,
        arrayOf(OpenableColumns.SIZE),
        null,
        null,
        null
    )

    val fileSize = cursor?.use {
        if (it.moveToFirst()) {
            it.getLong(it.getColumnIndexOrThrow(OpenableColumns.SIZE))
        } else {
            -1L
        }
    } ?: -1L

    val maxSize = 5 * 1024 * 1024L

    if (fileSize > maxSize) {
        return null
    }

    val bytes = contentResolver.openInputStream(uri)
        ?.use { it.readBytes() } ?: return null

    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        "File",
        "profile_picture",
        requestBody
    )
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
        )
    }
}

@Composable
private fun ProductCard(
    modifier: Modifier = Modifier,
    imageRes: Int,
    name: String,
    description: String,
    price: String,
    onViewItem: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = price,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onViewItem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "VIEW ITEM",
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun productImageResource(productName: String): Int {
    return when (productName.trim().lowercase()) {
        "caffe latte" -> R.drawable.caffe_latte
        "cappuccino" -> R.drawable.cappucino
        "americano" -> R.drawable.americano
        "espresso" -> R.drawable.espresso
        "flat white" -> R.drawable.flat_white
        "cortado" -> R.drawable.cortado
        "caffee mocha", "caffe mocha" -> R.drawable.caffee_mocha
        "iced caffe latte" -> R.drawable.iced_caffee_latte
        "iced caffe mocha" -> R.drawable.iced_caffe_mocha
        "iced coffee" -> R.drawable.iced_coffee
        "chai latte" -> R.drawable.chai_latte
        "green/chai/peppermint" -> R.drawable.green_chai_peppermint
        "rooibos/earl grey" -> R.drawable.rooibos_earl_grey
        "plain croissant" -> R.drawable.plain_croissant
        "almond croissant" -> R.drawable.almond_croissant
        "chocolate croissant" -> R.drawable.chocolate_croissant
        "ham & cheese croissant" -> R.drawable.ham_and_cheese_croissant
        "blueberry muffin" -> R.drawable.blue_muffin
        "double choc chip muffin" -> R.drawable.double_choc_chip__muffin
        "lemon & poppyseed muffin",
        "lemon and poppyseed muffin" -> R.drawable.lemon_and_poppyseed_muffin
        "chocolate ganache brownie" -> R.drawable.chocolate_ganache_brownie
        "salted caramel cheesecake" -> R.drawable.salted_caramel_cheesecake
        "strawberry cheesecake" -> R.drawable.strawberry_cheesecake
        "skinner cookie" -> R.drawable.skinner_cookie
        "cookie dough chocolate chip" -> R.drawable.cookie_dough_chocoloate_chip
        "hot chocolate" -> R.drawable.hot_chocolate
        "frosted caramel latte" -> R.drawable.frosted_caramel_latte
        "spanish latte" -> R.drawable.spanish_latte
        "coffee freezo" -> R.drawable.coffee_freezo
        "salted caramel freezo" -> R.drawable.salted_caramel_freezo
        "skinner freezo" -> R.drawable.skinner_freezo
        "s'mores freezo", "smores freezo" -> R.drawable.s_mores_freezo
        "toasted cheese bagel" -> R.drawable.toasted_cheese_bagel
        "yoghurt & muesli",
        "yoghurt and muesli" -> R.drawable.yoghurt_and_muesli
        "mountain falls sparkling 1l" -> R.drawable.mountain_falls_sparkling_1l
        "mountain falls still 500ml" -> R.drawable.mountain_falls_still_500ml
        "san pellegrino sodas" -> R.drawable.san_pellegrino_sodas
        "sir fruit fresh juices" -> R.drawable.sir_fruit_fresh_juices
        "boosters" -> R.drawable.boosters
        else -> R.drawable.skinner_cafe_logo
    }
}

@Composable
private fun CafeBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Home",
                selected = selectedTab == "Home",
                onClick = { onTabSelected("Home") }
            )

            BottomNavItem(
                label = "Menu",
                selected = selectedTab == "Menu",
                onClick = { onTabSelected("Menu") }
            )

            BottomNavItem(
                label = "Cart",
                selected = selectedTab == "Cart",
                onClick = { onTabSelected("Cart") }
            )

            BottomNavItem(
                label = "Profile",
                selected = selectedTab == "Profile",
                onClick = { onTabSelected("Profile") }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(25.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun SkinnerCafeMenuScreen(
    categories: List<MenuCategory>,
    products: List<MenuProduct>,
    isLoading: Boolean,
    loadError: String?,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onViewItem: (MenuProduct) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "SKINNER CAFE",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Our Menu",
            color = MaterialTheme.colorScheme.tertiary,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Something delicious for every moment.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    label = category.categoryName,
                    selected = category.categoryName.equals(
                        selectedCategory,
                        ignoreCase = true
                    ),
                    onClick = {
                        onCategorySelected(category.categoryName)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            isLoading -> {
                Text(
                    text = "Loading our menu...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            loadError != null -> {
                Text(
                    text = loadError,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 14.sp
                )
            }

            else -> {
                val filteredProducts = products.filter {
                    it.category.equals(
                        selectedCategory,
                        ignoreCase = true
                    )
                }

                Text(
                    text = selectedCategory,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${filteredProducts.size} items",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(16.dp))

                filteredProducts.chunked(2).forEach { productRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        productRow.forEach { product ->
                            ProductCard(
                                modifier = Modifier.weight(1f),
                                imageRes = productImageResource(
                                    product.productName
                                ),
                                name = product.productName,
                                description = product.description
                                    ?: "Freshly prepared at Skinner Cafe",
                                price = "From R ${
                                    String.format(
                                        java.util.Locale.US,
                                        "%.2f",
                                        product.variants.minOfOrNull {
                                            it.price
                                        } ?: 0.0
                                    )
                                }",
                                onViewItem = { onViewItem(product) }
                            )
                        }

                        if (productRow.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                if (filteredProducts.isEmpty()) {
                    Text(
                        text = "No products are available in this category yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SkinnerCafeHomePreview() {
    SkinnerCafeTheme {
        SkinnerCafeHome()
    }
}




@Composable
private fun ProductDetailsScreen(
    product: MenuProduct,
    onBack: () -> Unit,
    onAddToCart: (CartItem) -> Unit
) {
    var selectedVariantId by remember(product.productId) {
        mutableStateOf(product.variants.firstOrNull()?.variantId)
    }

    var selectedAddOnIds by remember(product.productId) {
        mutableStateOf(setOf<Int>())
    }

    val selectedVariant = product.variants.firstOrNull {
        it.variantId == selectedVariantId
    } ?: product.variants.firstOrNull()

    val selectedAddOns = product.addOns.filter {
        it.addOnId in selectedAddOnIds
    }

    val total = (selectedVariant?.price ?: 0.0) +
        selectedAddOns.sumOf { it.price }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("← BACK TO MENU")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(
                productImageResource(product.productName)
            ),
            contentDescription = product.productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = product.productName,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = product.category,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = product.description
                ?: "Freshly prepared at Skinner Cafe.",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose your size",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (product.variants.isEmpty()) {
            Text(
                text = "No sizes or variants available.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            product.variants.forEach { variant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedVariantId = variant.variantId
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVariantId == variant.variantId,
                            onClick = {
                                selectedVariantId = variant.variantId
                            }
                        )

                        Text(
                            text = variant.variantName,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "R ${
                            String.format(
                                java.util.Locale.US,
                                "%.2f",
                                variant.price
                            )
                        }",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Add-ons",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (product.addOns.isEmpty()) {
            Text(
                text = "No add-ons available for this product.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            product.addOns.forEach { addOn ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = addOn.addOnId in selectedAddOnIds,
                            onCheckedChange = { checked ->
                                selectedAddOnIds =
                                    if (checked) {
                                        selectedAddOnIds + addOn.addOnId
                                    } else {
                                        selectedAddOnIds - addOn.addOnId
                                    }
                            }
                        )

                        Text(
                            text = addOn.addOnName,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "+ R ${
                            String.format(
                                java.util.Locale.US,
                                "%.2f",
                                addOn.price
                            )
                        }",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Your total",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "R ${
                        String.format(
                            java.util.Locale.US,
                            "%.2f",
                            total
                        )
                    }",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onAddToCart(
                    CartItem(
                        product = product,
                        variant = selectedVariant,
                        addOns = selectedAddOns,
                        quantity = 1
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = "ADD TO CART",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CartScreen(
    cartItems: List<CartItem>,
    onQuantityChange: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCheckout: () -> Unit
) {
    val subtotal = cartItems.sumOf { it.totalPrice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Your Cart",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cartItems.isEmpty()) {
            Text(
                text = "Your cart is currently empty.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            cartItems.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = item.product.productName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        item.variant?.let {
                            Text(
                                text = "Size: ${it.variantName}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (item.addOns.isNotEmpty()) {
                            Text(
                                text = "Add-ons: ${
                                    item.addOns.joinToString { it.addOnName }
                                }",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Unit price: R${"%.2f".format(item.unitPrice)}",
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (item.quantity > 1) {
                                            onQuantityChange(
                                                index,
                                                item.quantity - 1
                                            )
                                        }
                                    }
                                ) {
                                    Text("-")
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                TextButton(
                                    onClick = {
                                        onQuantityChange(
                                            index,
                                            item.quantity + 1
                                        )
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            Text(
                                text = "R${"%.2f".format(item.totalPrice)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(
                            onClick = { onRemoveItem(index) }
                        ) {
                            Text(
                                text = "REMOVE ITEM",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "R${"%.2f".format(subtotal)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = cartItems.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "PROCEED TO CHECKOUT",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}