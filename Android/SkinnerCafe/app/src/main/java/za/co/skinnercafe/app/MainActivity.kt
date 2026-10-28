package za.co.skinnercafe.app

import android.os.Bundle
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

private val CafeGreen = Color(0xFF315744)
private val CafeDarkGreen = Color(0xFF183D2C)
private val CafeCream = Color(0xFFF7F4EC)
private val CafeWhite = Color(0xFFFFFEFA)
private val CafeText = Color(0xFF292D27)
private val CafeMuted = Color(0xFF77796F)
private val CafeSage = Color(0xFFE4E9DF)
private val CafeGold = Color(0xFFD8BB83)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkinnerCafeTheme {
                SkinnerCafeHome()
            }
        }
    }
}

@Composable
fun SkinnerCafeHome() {
    var categories by remember { mutableStateOf<List<MenuCategory>>(emptyList()) }
    var products by remember { mutableStateOf<List<MenuProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("Coffee") }
    var selectedTab by remember { mutableStateOf("Home") }


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
        containerColor = CafeCream,
        bottomBar = { CafeBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
            }
        ) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == "Menu") {
                SkinnerCafeMenuScreen(
                    categories = categories,
                    products = products,
                    isLoading = isLoading,
                    loadError = loadError,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CafeCream)
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
                        color = CafeMuted,
                        fontSize = 10.sp,
                        letterSpacing = 1.7.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = CafeSage,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SC",
                            color = CafeDarkGreen,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
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
                        .background(CafeDarkGreen.copy(alpha = 0.76f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MADE FOR YOUR MOMENT",
                        color = CafeGold,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Good coffee.\nBetter moments.",
                        color = CafeWhite,
                        fontSize = 31.sp,
                        lineHeight = 37.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Your favourite cup is waiting.",
                        color = CafeWhite.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { selectedTab = "Menu" },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CafeCream,
                            contentColor = CafeDarkGreen
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
                        color = CafeText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Worth coming back for.",
                        color = CafeMuted,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "OUR PICKS",
                    color = CafeGreen,
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
                        }"
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Brand statement
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = CafeSage,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "A moment to slow down.",
                    color = CafeDarkGreen,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "From your first morning coffee to an afternoon treat, " +
                            "make a little room for something delicious.",
                    color = CafeMuted,
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


@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = CafeText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = CafeMuted,
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
        color = if (selected) CafeDarkGreen else CafeWhite,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) CafeWhite else CafeText,
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
    price: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = CafeWhite
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
                color = CafeText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = description,
                color = CafeMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = price,
                color = CafeDarkGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeGreen,
                    contentColor = CafeWhite
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
        color = CafeWhite,
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
                    if (selected) CafeGreen else Color.Transparent
                )
        )

        Spacer(Modifier.height(7.dp))

        Text(
            text = label,
            color = if (selected) CafeDarkGreen else CafeMuted,
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
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "SKINNER CAFE",
            color = CafeGreen,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Our Menu",
            color = CafeDarkGreen,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Something delicious for every moment.",
            color = CafeMuted,
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
                    color = CafeMuted,
                    fontSize = 14.sp
                )
            }

            loadError != null -> {
                Text(
                    text = loadError,
                    color = CafeDarkGreen,
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
                    color = CafeDarkGreen,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${filteredProducts.size} items",
                    color = CafeMuted,
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
                                }"
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
                        color = CafeMuted,
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
