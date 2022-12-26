package com.smileidentity.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smileidentity.ui.compose.SelfieCaptureOrPermissionScreen
import com.smileidentity.ui.core.SelfieCaptureResult
import timber.log.Timber

@Preview
@Composable
fun ProductSelectionScreen(onProductSelected: (Screen) -> Unit = {}) {
    val products = listOf(Screen.SmartSelfie)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            stringResource(R.string.test_our_products),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(products) {
                Card(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(onClick = { onProductSelected(it) }),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Image(
                            Icons.Default.Face,
                            stringResource(R.string.product_name_icon, stringResource(it.label)),
                        )
                        Text(stringResource(it.label), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelfieCaptureScreen() {
    val context = LocalContext.current
    SelfieCaptureOrPermissionScreen(true) {
        if (it is SelfieCaptureResult.Success) {
            val message = "Image captured successfully: ${it.selfieFile}"
            context.toast(message)
            Timber.d(message)
        } else if (it is SelfieCaptureResult.Error) {
            val message = "Image capture error: $it"
            context.toast(message)
            Timber.e(it.throwable, message)
        }
    }
}
