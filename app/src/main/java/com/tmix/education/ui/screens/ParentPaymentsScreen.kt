package com.tmix.education.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tmix.education.data.model.Payment
import com.tmix.education.data.model.PaymentStatus
import com.tmix.education.ui.theme.*
import com.tmix.education.ui.viewmodel.ParentDashboardViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Parent Payments Screen with VietQR
 * Connected to real backend data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPaymentsScreen(
    viewModel: ParentDashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val payments = state.payments
    var showQRSheet by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<Payment?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Calculate totals from real data
    val totalPaid = payments.sumOf { it.paidAmount }.toLong()
    val totalRemaining = payments.filter { !it.isPaid }.sumOf { it.remainingAmount }.toLong()
    val totalDiscount = payments.sumOf { it.discountAmount }.toLong()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán học phí") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TMixNavy,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TMixRed)
                }
            }
            payments.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null, Modifier.size(80.dp), tint = TextSecondary.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Chưa có hóa đơn nào", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text("Hóa đơn sẽ hiển thị khi có phát sinh học phí", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryCard(Modifier.weight(1f), "Đã thanh toán", totalPaid, SuccessLight, Success)
                            SummaryCard(Modifier.weight(1f), "Còn nợ", totalRemaining, ErrorLight, Error)
                            SummaryCard(Modifier.weight(1f), "Giảm giá", totalDiscount, WarningLight, Warning)
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Danh sách hóa đơn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    items(payments) { payment ->
                        PaymentInvoiceCard(payment) {
                            selectedPayment = payment
                            showQRSheet = true
                        }
                    }
                }
            }
        }

        // QR Bottom Sheet
        if (showQRSheet && selectedPayment != null) {
            ModalBottomSheet(
                onDismissRequest = { showQRSheet = false },
                sheetState = sheetState,
                shape = TMixShapes.BottomSheet
            ) {
                PaymentQRSheetContent(selectedPayment!!) { showQRSheet = false }
            }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier, label: String, amount: Long, bgColor: Color, textColor: Color) {
    Card(modifier = modifier, shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(formatPaymentCurrency(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun PaymentInvoiceCard(payment: Payment, onPayClick: () -> Unit) {
    val studentName = payment.student?.name ?: "Học sinh"
    val className = payment.classInfo?.name ?: "Lớp học"
    val monthYear = "Tháng ${payment.month}/${payment.year}"
    val remainingAmount = payment.remainingAmount.toLong()

    Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("$className - $monthYear", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                PaymentStatusChip(payment.status)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Còn lại", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        formatPaymentCurrency(remainingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!payment.isPaid) Error else Success
                    )
                }
                if (!payment.isPaid) {
                    Button(onClick = onPayClick, shape = TMixShapes.Button, colors = ButtonDefaults.buttonColors(containerColor = TMixRed)) {
                        Icon(Icons.Filled.QrCode2, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Thanh toán")
                    }
                }
            }

            // Show payment history if any
            if (!payment.histories.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Đã thanh toán ${payment.histories.size} lần — ${formatPaymentCurrency(payment.paidAmount.toLong())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success
                )
            }
        }
    }
}

@Composable
fun PaymentStatusChip(status: String) {
    val (bg, text, label) = when (status) {
        PaymentStatus.PAID -> Triple(SuccessLight, Success, "Đã thanh toán")
        PaymentStatus.PARTIAL -> Triple(WarningLight, Warning, "Một phần")
        PaymentStatus.OVERDUE -> Triple(ErrorLight, Error, "Quá hạn")
        else -> Triple(InfoLight, Info, "Chờ thanh toán")
    }
    Surface(color = bg, shape = TMixShapes.Chip) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = text)
    }
}

@Composable
fun PaymentQRSheetContent(payment: Payment, onClose: () -> Unit) {
    val amount = payment.remainingAmount.toLong()
    val qrUrl = "https://img.vietqr.io/image/MB-0346857241-compact.png?amount=${amount}&addInfo=TMIX-${payment.id}"

    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Quét mã QR để thanh toán", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Đóng") }
        }

        Spacer(Modifier.height(16.dp))

        // Payment info
        Card(shape = TMixShapes.Card, colors = CardDefaults.cardColors(containerColor = SurfaceVariant)) {
            Column(Modifier.padding(12.dp)) {
                Text("Học sinh: ${payment.student?.name ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Lớp: ${payment.classInfo?.name ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Tháng ${payment.month}/${payment.year}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(shape = TMixShapes.Card, elevation = CardDefaults.cardElevation(8.dp)) {
            AsyncImage(
                model = qrUrl,
                contentDescription = "VietQR",
                modifier = Modifier.size(250.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(formatPaymentCurrency(amount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TMixRed)

        Spacer(Modifier.height(8.dp))

        Text("Mở app ngân hàng và quét mã QR để thanh toán", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(Modifier.height(32.dp))
    }
}

private fun formatPaymentCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}
