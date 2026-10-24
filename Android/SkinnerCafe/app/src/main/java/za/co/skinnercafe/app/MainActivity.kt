
package za.co.skinnercafe.app

import android.os.Bundle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CafeCream,
        bottomBar = { CafeBottomNavigation() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        onClick = { },
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
                CategoryChip("Coffee", selected = true)
                CategoryChip("Iced drinks")
                CategoryChip("Bakery")
                CategoryChip("Sweet treats")
                CategoryChip("Tea")
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
                ProductCard(
                    modifier = Modifier.weight(1f),
                    imageRes = R.drawable.iced_caffee_latte,
                    name = "Iced Latte",
                    description = "Cool, smooth & creamy",
                    price = "R 45"
                )

                ProductCard(
                    modifier = Modifier.weight(1f),
                    imageRes = R.drawable.cappucino,
                    name = "Cappuccino",
                    description = "Rich, velvety coffee",
                    price = "R 35"
                )
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
    selected: Boolean = false
) {
    Surface(
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

@Composable
private fun CafeBottomNavigation() {
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
            BottomNavItem("Home", selected = true)
            BottomNavItem("Menu")
            BottomNavItem("Cart")
            BottomNavItem("Profile")
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean = false
) {
    Column(
        modifier = Modifier.width(68.dp),
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

@Preview(showBackground = true)
@Composable
private fun SkinnerCafeHomePreview() {
    SkinnerCafeTheme {
        SkinnerCafeHome()
    }
}
