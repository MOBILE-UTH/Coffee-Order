package com.coffee.order.base.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coffee.order.fragment.OrderFragment
import com.coffee.order.viewmodel.model.MenuItem
import java.text.DecimalFormat


@Preview
@Composable
fun SelectMenuItemBottomSheetPreview() {
    val sampleCart = OrderFragment.Cart(tableId = 5).apply {
        addItem(menuItemId = 1, quantity = 2)
        addItem(menuItemId = 2, quantity = 1)
    }
    val sampleMenuItems = listOf(
        MenuItem(menuItemId = 1, name = "Espresso", category = "Coffee", price = 30000.0),
        MenuItem(menuItemId = 2, name = "Cappuccino", category = "Coffee", price = 35000.0),
        MenuItem(menuItemId = 3, name = "Green Tea", category = "Tea", price = 25000.0),
        MenuItem(menuItemId = 4, name = "Lemonade", category = "Juice", price = 20000.0),
    )
    SelectMenuItemBottomSheet(
        cart = sampleCart,
        menuItems = sampleMenuItems,
        onDismiss = {},
        onConfirm = {}
    )
}

@Composable
fun SelectMenuItemBottomSheet(
    cart: OrderFragment.Cart,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    // State để trigger recomposition khi số lượng món thay đổi
    var cartTrigger by remember { mutableIntStateOf(0) }

    val groupedItems = menuItems.groupBy { it.category }
    val totalPrice = menuItems.sumOf { (cart.getItemQuantity(it.menuItemId)) * it.price }
    val totalItems = menuItems.sumOf { cart.getItemQuantity(it.menuItemId) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "New Order – Table ${cart.tableId}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                Text(
                    text = "Select items to add to ticket", fontSize = 14.sp, color = Color.Gray
                )
            }
            IconButton(
                onClick = onDismiss, modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu List
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedItems.forEach { (category, items) ->
                item {
                    Text(
                        text = category.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items) { item ->
                    MenuItemRow(
                        item = item,
                        quantity = cart.getItemQuantity(item.menuItemId),
                        onAdd = {
                            cart.addItem(item.menuItemId)
                            cartTrigger++
                        },
                        onRemove = {
                            cart.removeItem(item.menuItemId)
                            cartTrigger++
                        })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtotal ($totalItems items)", fontSize = 16.sp, color = Color.Gray
            )
            Text(
                text = "${formatPrice(totalPrice)}đ", fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00623B)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Confirm Order", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

fun ByteArray.toImageBitmap(): ImageBitmap {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    return bitmap.asImageBitmap()
}

@Composable
fun MenuItemRow(
    item: MenuItem, quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh minh họa (Thay bằng icon/image thực tế của bạn)
            val byteArray = item.image
            if (byteArray != null) {
                Image(
                    bitmap = byteArray.toImageBitmap(),
                    contentDescription = "Menu Item Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_report_image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${formatPrice(item.price)}đ",
                    color = Color(0xFF00623B),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bộ tăng giảm số lượng
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$quantity",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF00623B))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatPrice(price: Double): String {
    return DecimalFormat("#,###").format(price)
}