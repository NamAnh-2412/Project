package com.nanh.quanlybanhang.ui.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nanh.quanlybanhang.QuanLyBanHangApp
import com.nanh.quanlybanhang.R
import com.nanh.quanlybanhang.data.local.CatalogDao
import com.nanh.quanlybanhang.data.local.CategoryEntity
import com.nanh.quanlybanhang.data.local.CustomerEntity
import com.nanh.quanlybanhang.data.local.OptionGroupEntity
import com.nanh.quanlybanhang.data.local.OptionItemEntity
import com.nanh.quanlybanhang.data.local.OrderDao
import com.nanh.quanlybanhang.data.local.OrderEntity
import com.nanh.quanlybanhang.data.local.OrderLineEntity
import com.nanh.quanlybanhang.data.local.OrderStatus
import com.nanh.quanlybanhang.data.local.ProductEntity
import com.nanh.quanlybanhang.data.local.ProductOptionGroupEntity
import com.nanh.quanlybanhang.data.local.ProductScope
import com.nanh.quanlybanhang.util.formatVndCompact
import com.nanh.quanlybanhang.util.formatVndGrouped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

private const val FIRST_DRAFT_ID = 1L

enum class PlaceOrderCustomerKind {
    RETAIL,
    REGULAR,
}

data class OptionItemPickUi(val id: Long, val name: String, val extraPrice: Double)

data class LinkedOptionGroupForPick(
    val groupId: Long,
    val groupName: String,
    val items: List<OptionItemPickUi>,
)

data class PlaceOrderCategoryUi(val id: Long, val label: String)

data class PlaceOrderProductUi(
    val id: Long,
    val name: String,
    val priceLabel: String,
    val categoryId: Long,
    val unitPrice: Double,
    val imageLocalPath: String?,
    val linkedOptionGroups: List<LinkedOptionGroupForPick>,
)

data class DraftTabUi(
    val id: Long,
    val label: String,
    val selected: Boolean,
    val showClose: Boolean,
)

data class CustomerPickUi(
    val id: Long,
    val name: String,
    val phone: String?,
) {
    val label: String
        get() {
            val p = phone?.trim()?.takeIf { it.isNotEmpty() }
            return if (p != null) "$name · $p" else name
        }
}

data class CartLineUi(
    val cartLineKey: String,
    val productId: Long,
    val name: String,
    val quantity: Int,
    val lineTotalLabel: String,
    val selectionSummary: String?,
    val selectedOptionItemIds: List<Long>,
)

data class PlaceOrderUiState(
    val categories: List<PlaceOrderCategoryUi>,
    val selectedCategoryIndex: Int,
    val products: List<PlaceOrderProductUi>,
    val draftTabs: List<DraftTabUi>,
    val cartLines: List<CartLineUi>,
    val cartSummaryLabel: String,
    /** Tổng tiền đơn nháp đang chọn (VNĐ). */
    val cartTotalMoney: Double,
    /** Nhãn tổng (vd 285K) cho cột đơn. */
    val cartTotalFormatted: String,
    /** Tổng số lượng món (cộng dồn quantity). */
    val cartItemQty: Int,
    /** Khách lẻ / khách quen. */
    val customerKind: PlaceOrderCustomerKind,
    /** Danh sách khách (khách quen). */
    val customers: List<CustomerPickUi>,
    /** Khách đang chọn khi loại = khách quen (null = chưa chọn). */
    val selectedCustomerId: Long?,
)

private data class DraftOrdersState(
    val sessions: List<DraftSession>,
    val selectedId: Long,
)

private data class DraftSession(
    val id: Long,
    val cartLines: List<DraftCartLine>,
)

private data class DraftCartLine(
    val productId: Long,
    val name: String,
    val unitPrice: Double,
    val quantity: Int,
    val selectedOptionItemIds: List<Long>,
    val selectionSummary: String,
)

private data class CatalogDraftCustomerCore(
    val catalog: CatalogParts,
    val selCatId: Long?,
    val query: String,
    val drafts: DraftOrdersState,
    val customerKind: PlaceOrderCustomerKind,
    val selectedCustomerId: Long?,
)

private data class CatalogDraftWithoutSelectedCustomer(
    val catalog: CatalogParts,
    val selCatId: Long?,
    val query: String,
    val drafts: DraftOrdersState,
    val customerKind: PlaceOrderCustomerKind,
)

sealed interface CheckoutPersistResult {
    data object Success : CheckoutPersistResult
    data class Error(val message: String) : CheckoutPersistResult
}

private data class CatalogParts(
    val products: List<ProductEntity>,
    val categories: List<CategoryEntity>,
    val links: List<ProductOptionGroupEntity>,
    val groups: List<OptionGroupEntity>,
    val items: List<OptionItemEntity>,
)

class PlaceOrderViewModel(
    application: Application,
    private val orderDao: OrderDao,
    private val catalogDao: CatalogDao,
) : AndroidViewModel(application) {

    private val catalogSnapshot = AtomicReference<CatalogParts?>(null)

    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val searchQuery = MutableStateFlow("")
    private val draftOrders = MutableStateFlow(
        DraftOrdersState(
            sessions = listOf(DraftSession(id = FIRST_DRAFT_ID, cartLines = emptyList())),
            selectedId = FIRST_DRAFT_ID,
        ),
    )

    private val customerKind = MutableStateFlow(PlaceOrderCustomerKind.RETAIL)
    private val selectedCustomerId = MutableStateFlow<Long?>(null)

    private val productsFlow = catalogDao.observeProductsByScope(ProductScope.SALE).flowOn(Dispatchers.IO)
    private val categoriesFlow = catalogDao.observeCategories().flowOn(Dispatchers.IO)
    private val linksFlow = catalogDao.observeProductOptionLinks().flowOn(Dispatchers.IO)
    private val optionGroupsFlow = catalogDao.observeOptionGroups().flowOn(Dispatchers.IO)
    private val optionItemsFlow = catalogDao.observeOptionItemsAll().flowOn(Dispatchers.IO)
    private val customersFlow = orderDao.observeCustomers().flowOn(Dispatchers.IO)

    private val catalogFlow = combine(
        productsFlow,
        categoriesFlow,
        linksFlow,
        optionGroupsFlow,
        optionItemsFlow,
    ) { products, cats, links, groups, items ->
        CatalogParts(products, cats, links, groups, items)
    }.flowOn(Dispatchers.Default)

    private val corePlaceOrderFlow = combine(
        catalogFlow,
        selectedCategoryId,
        searchQuery,
        draftOrders,
        customerKind,
    ) { catalog, selCatId, query, drafts, ck ->
        CatalogDraftWithoutSelectedCustomer(catalog, selCatId, query, drafts, ck)
    }.combine(selectedCustomerId) { partial, scid ->
        CatalogDraftCustomerCore(
            partial.catalog,
            partial.selCatId,
            partial.query,
            partial.drafts,
            partial.customerKind,
            scid,
        )
    }.flowOn(Dispatchers.Default)

    val uiState = combine(corePlaceOrderFlow, customersFlow) { core, customerEntities ->
        catalogSnapshot.set(core.catalog)
        mapPlaceOrderUi(
            app = getApplication(),
            catalog = core.catalog,
            selCatId = core.selCatId,
            query = core.query,
            drafts = core.drafts,
            customerKind = core.customerKind,
            customerRows = customerEntities.map { it.toCustomerPickUi() },
            selectedCustomerId = core.selectedCustomerId,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PlaceOrderUiState(
                categories = emptyList(),
                selectedCategoryIndex = 0,
                products = emptyList(),
                draftTabs = emptyList(),
                cartLines = emptyList(),
                cartSummaryLabel = "",
                cartTotalMoney = 0.0,
                cartTotalFormatted = "0",
                cartItemQty = 0,
                customerKind = PlaceOrderCustomerKind.RETAIL,
                customers = emptyList(),
                selectedCustomerId = null,
            ),
        )

    fun selectCategory(categoryId: Long) {
        selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(q: String) {
        searchQuery.value = q
    }

    fun setCustomerKind(kind: PlaceOrderCustomerKind) {
        customerKind.value = kind
        if (kind == PlaceOrderCustomerKind.RETAIL) {
            selectedCustomerId.value = null
        }
    }

    fun setSelectedCustomerId(id: Long?) {
        selectedCustomerId.value = id
    }

    suspend fun quickAddCustomer(name: String, phone: String?): Long {
        val n = name.trim()
        require(n.isNotEmpty())
        return withContext(Dispatchers.IO) {
            orderDao.insertCustomer(
                CustomerEntity(
                    name = n,
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                ),
            )
        }
    }

    suspend fun completeCheckoutAfterPayment(
        paymentMethodIndex: Int,
        customerPayDong: Long,
    ): CheckoutPersistResult {
        val app = getApplication<Application>()
        return withContext(Dispatchers.IO) {
            val drafts = draftOrders.value
            val selId = drafts.selectedId
            val session = drafts.sessions.firstOrNull { it.id == selId }
                ?: return@withContext CheckoutPersistResult.Error(app.getString(R.string.checkout_persist_error))
            if (session.cartLines.isEmpty()) {
                return@withContext CheckoutPersistResult.Error(app.getString(R.string.place_order_checkout_need_lines))
            }

            val kind = customerKind.value
            val custPick = selectedCustomerId.value
            val customersSnap = orderDao.listCustomersSorted()

            when (kind) {
                PlaceOrderCustomerKind.REGULAR -> {
                    if (customersSnap.isEmpty()) {
                        return@withContext CheckoutPersistResult.Error(
                            app.getString(R.string.checkout_no_customers),
                        )
                    }
                    if (custPick == null || customersSnap.none { it.id == custPick }) {
                        return@withContext CheckoutPersistResult.Error(
                            app.getString(R.string.checkout_pick_customer),
                        )
                    }
                }
                PlaceOrderCustomerKind.RETAIL -> Unit
            }

            val orderCustomerId = when (kind) {
                PlaceOrderCustomerKind.RETAIL -> null
                PlaceOrderCustomerKind.REGULAR -> custPick
            }

            val total = session.cartLines.sumOf { it.unitPrice * it.quantity }
            val safeMethod = paymentMethodIndex.coerceIn(
                0,
                (CheckoutPaymentMethods.labelResIds.size - 1).coerceAtLeast(0),
            )
            val order = OrderEntity(
                createdAtEpochMillis = System.currentTimeMillis(),
                customerId = orderCustomerId,
                status = OrderStatus.COMPLETED,
                totalAmount = total,
                paymentMethodIndex = safeMethod,
                customerPayAmount = customerPayDong.toDouble(),
            )
            val lines = session.cartLines.map { line ->
                OrderLineEntity(
                    orderId = 0L,
                    productId = line.productId,
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                    lineTotal = line.unitPrice * line.quantity,
                    lineNote = line.selectionSummary.trim().takeIf { it.isNotEmpty() },
                )
            }

            orderDao.insertOrderWithLines(order, lines)

            draftOrders.update { st ->
                st.copy(
                    sessions = st.sessions.map { sess ->
                        if (sess.id != selId) sess else sess.copy(cartLines = emptyList())
                    },
                )
            }
            CheckoutPersistResult.Success
        }
    }

    fun selectDraftTab(id: Long) {
        draftOrders.update { st ->
            if (st.sessions.none { it.id == id }) st else st.copy(selectedId = id)
        }
    }

    fun closeDraftTab(id: Long) {
        draftOrders.update { st ->
            if (st.sessions.size <= 1) return@update st
            val rest = st.sessions.filter { it.id != id }
            val newSel = if (st.selectedId != id) st.selectedId else rest.first().id
            st.copy(sessions = rest, selectedId = newSel)
        }
    }

    fun tryAddDraft(): Boolean {
        val snapshot = draftOrders.value
        if (snapshot.sessions.size >= MAX_DRAFT_SESSIONS) return false
        val newId = (snapshot.sessions.maxOfOrNull { it.id } ?: 0L) + 1L
        draftOrders.value = snapshot.copy(
            sessions = snapshot.sessions + DraftSession(newId, emptyList()),
            selectedId = newId,
        )
        return true
    }

    fun addProductToSelectedDraft(product: PlaceOrderProductUi, selectedOptionItemIds: List<Long>) {
        val sortedUnique = selectedOptionItemIds.distinct().sorted()
        val extras = extrasForSelection(product, sortedUnique)
        val lineUnit = product.unitPrice + extras
        val summary = buildSelectionSummary(product, sortedUnique)

        draftOrders.update { st ->
            val selId = st.selectedId
            st.copy(
                sessions = st.sessions.map { session ->
                    if (session.id != selId) return@map session
                    val idx = session.cartLines.indexOfFirst {
                        it.productId == product.id && it.selectedOptionItemIds == sortedUnique
                    }
                    val newLines = if (idx >= 0) {
                        session.cartLines.mapIndexed { i, line ->
                            if (i == idx) line.copy(quantity = line.quantity + 1) else line
                        }
                    } else {
                        session.cartLines + DraftCartLine(
                            productId = product.id,
                            name = product.name,
                            unitPrice = lineUnit,
                            quantity = 1,
                            selectedOptionItemIds = sortedUnique,
                            selectionSummary = summary,
                        )
                    }
                    session.copy(cartLines = newLines)
                },
            )
        }
    }

    fun clearSelectedDraftCart() {
        draftOrders.update { st ->
            st.copy(
                sessions = st.sessions.map { session ->
                    if (session.id != st.selectedId) session else session.copy(cartLines = emptyList())
                },
            )
        }
    }

    /** Lấy đủ dữ liệu sản phẩm để mở dialog nhóm lựa chọn (sửa dòng đơn), không phụ thuộc danh mục đang xem. */
    fun productUiForEdit(productId: Long): PlaceOrderProductUi? {
        val catalog = catalogSnapshot.get() ?: return null
        val p = catalog.products.find { it.id == productId } ?: return null
        val linked = buildLinkedGroupsForProduct(p.id, catalog)
        return PlaceOrderProductUi(
            id = p.id,
            name = p.name,
            priceLabel = formatVndCompact(p.unitPrice),
            categoryId = p.categoryId,
            unitPrice = p.unitPrice,
            imageLocalPath = p.imageLocalPath,
            linkedOptionGroups = linked,
        )
    }

    /** Thay nhóm lựa chọn một dòng đơn (cùng SL), gộp dòng nếu trùng món + cùng lựa chọn. */
    fun replaceDraftLineOptions(cartLineKey: String, product: PlaceOrderProductUi, selectedOptionItemIds: List<Long>) {
        val sortedUnique = selectedOptionItemIds.distinct().sorted()
        val extras = extrasForSelection(product, sortedUnique)
        val lineUnit = product.unitPrice + extras
        val summary = buildSelectionSummary(product, sortedUnique)

        draftOrders.update { st ->
            val selId = st.selectedId
            st.copy(
                sessions = st.sessions.map { session ->
                    if (session.id != selId) return@map session
                    val oldIdx = session.cartLines.indexOfFirst { draftLineKey(it) == cartLineKey }
                    if (oldIdx < 0) return@map session
                    val old = session.cartLines[oldIdx]
                    val qty = old.quantity
                    val rest = session.cartLines.filterIndexed { i, _ -> i != oldIdx }
                    val newLine = DraftCartLine(
                        productId = product.id,
                        name = product.name,
                        unitPrice = lineUnit,
                        quantity = qty,
                        selectedOptionItemIds = sortedUnique,
                        selectionSummary = summary,
                    )
                    val dupIdx = rest.indexOfFirst {
                        it.productId == newLine.productId && it.selectedOptionItemIds == newLine.selectedOptionItemIds
                    }
                    val merged = if (dupIdx >= 0) {
                        rest.mapIndexed { i, l ->
                            if (i == dupIdx) l.copy(quantity = l.quantity + qty) else l
                        }
                    } else {
                        rest + newLine
                    }
                    session.copy(cartLines = merged)
                },
            )
        }
    }

    fun removeDraftLine(cartLineKey: String) {
        draftOrders.update { st ->
            val selId = st.selectedId
            st.copy(
                sessions = st.sessions.map { session ->
                    if (session.id != selId) return@map session
                    session.copy(
                        cartLines = session.cartLines.filter { draftLineKey(it) != cartLineKey },
                    )
                },
            )
        }
    }

    fun changeDraftLineQuantity(cartLineKey: String, delta: Int) {
        if (delta == 0) return
        draftOrders.update { st ->
            val selId = st.selectedId
            st.copy(
                sessions = st.sessions.map { session ->
                    if (session.id != selId) return@map session
                    val idx = session.cartLines.indexOfFirst { draftLineKey(it) == cartLineKey }
                    if (idx < 0) return@map session
                    val line = session.cartLines[idx]
                    val newQty = line.quantity + delta
                    val newLines = when {
                        newQty <= 0 -> session.cartLines.filterIndexed { i, _ -> i != idx }
                        else -> session.cartLines.mapIndexed { i, l ->
                            if (i == idx) l.copy(quantity = newQty) else l
                        }
                    }
                    session.copy(cartLines = newLines)
                },
            )
        }
    }

    companion object {
        const val MAX_DRAFT_SESSIONS = 8

        fun factory(app: QuanLyBanHangApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaceOrderViewModel(app, app.orderDao, app.catalogDao) as T
                }
            }
    }
}

private fun draftLineKey(line: DraftCartLine): String =
    "${line.productId}_${line.selectedOptionItemIds.joinToString(",")}"

private fun extrasForSelection(product: PlaceOrderProductUi, selectedIds: List<Long>): Double {
    val set = selectedIds.toSet()
    return product.linkedOptionGroups.asSequence()
        .flatMap { it.items.asSequence() }
        .filter { it.id in set }
        .sumOf { it.extraPrice }
}

private fun buildSelectionSummary(product: PlaceOrderProductUi, selectedIds: List<Long>): String {
    val set = selectedIds.toSet()
    return product.linkedOptionGroups.mapNotNull { g ->
        val picked = g.items.find { it.id in set }
        if (picked != null) "${g.groupName}: ${picked.name}" else null
    }.joinToString(", ")
}

private fun CustomerEntity.toCustomerPickUi(): CustomerPickUi {
    val p = phone?.trim()?.takeIf { it.isNotEmpty() }
    return CustomerPickUi(id = id, name = name, phone = p)
}

private fun buildLinkedGroupsForProduct(
    productId: Long,
    catalog: CatalogParts,
): List<LinkedOptionGroupForPick> {
    val groupIds = catalog.links
        .filter { it.productId == productId }
        .sortedBy { it.sortOrder }
        .map { it.optionGroupId }
    val groupsById = catalog.groups.associateBy { it.id }
    val itemsByGroup = catalog.items.groupBy { it.optionGroupId }
    return groupIds.mapNotNull { gid ->
        val g = groupsById[gid] ?: return@mapNotNull null
        val opts = itemsByGroup[gid].orEmpty().sortedBy { it.name.lowercase() }
        if (opts.isEmpty()) return@mapNotNull null
        LinkedOptionGroupForPick(
            groupId = gid,
            groupName = g.name,
            items = opts.map {
                OptionItemPickUi(id = it.id, name = it.name, extraPrice = it.extraPrice)
            },
        )
    }
}

private fun mapPlaceOrderUi(
    app: Application,
    catalog: CatalogParts,
    selCatId: Long?,
    query: String,
    drafts: DraftOrdersState,
    customerKind: PlaceOrderCustomerKind,
    customerRows: List<CustomerPickUi>,
    selectedCustomerId: Long?,
): PlaceOrderUiState {
    val sortedCats = catalog.categories.sortedBy { it.name.lowercase() }
    val categoryUis = sortedCats.map { PlaceOrderCategoryUi(id = it.id, label = it.name) }

    val effectiveCatId = when {
        selCatId != null && sortedCats.any { it.id == selCatId } -> selCatId
        else -> sortedCats.firstOrNull()?.id
    }

    val selectedIndex =
        sortedCats.indexOfFirst { it.id == effectiveCatId }.let { if (it < 0) 0 else it }

    val q = query.trim()
    val products = catalog.products
        .asSequence()
        .filter { effectiveCatId != null && it.categoryId == effectiveCatId }
        .filter { p -> q.isEmpty() || p.name.contains(q, ignoreCase = true) }
        .sortedBy { it.name.lowercase() }
        .map { p ->
            val linked = buildLinkedGroupsForProduct(p.id, catalog)
            PlaceOrderProductUi(
                id = p.id,
                name = p.name,
                priceLabel = formatVndCompact(p.unitPrice),
                categoryId = p.categoryId,
                unitPrice = p.unitPrice,
                imageLocalPath = p.imageLocalPath,
                linkedOptionGroups = linked,
            )
        }
        .toList()

    val selectedSession = drafts.sessions.firstOrNull { it.id == drafts.selectedId }
        ?: drafts.sessions.first()

    val cartLines = selectedSession.cartLines.map { line ->
        val key = draftLineKey(line)
        CartLineUi(
            cartLineKey = key,
            productId = line.productId,
            name = line.name,
            quantity = line.quantity,
            lineTotalLabel = formatVndGrouped(line.unitPrice * line.quantity),
            selectionSummary = line.selectionSummary.takeIf { it.isNotBlank() },
            selectedOptionItemIds = line.selectedOptionItemIds,
        )
    }
    val qtySum = selectedSession.cartLines.sumOf { it.quantity }
    val totalMoney = selectedSession.cartLines.sumOf { it.unitPrice * it.quantity }
    val cartTotalFormatted = formatVndGrouped(totalMoney)
    val cartSummaryLabel = app.getString(
        R.string.place_order_cart_summary,
        cartTotalFormatted,
        qtySum,
    )

    val tabUis = drafts.sessions.mapIndexed { index, s ->
        DraftTabUi(
            id = s.id,
            label = app.getString(R.string.place_order_draft_label, index + 1),
            selected = s.id == drafts.selectedId,
            showClose = drafts.sessions.size > 1,
        )
    }

    return PlaceOrderUiState(
        categories = categoryUis,
        selectedCategoryIndex = selectedIndex.coerceAtLeast(0),
        products = products,
        draftTabs = tabUis,
        cartLines = cartLines,
        cartSummaryLabel = cartSummaryLabel,
        cartTotalMoney = totalMoney,
        cartTotalFormatted = cartTotalFormatted,
        cartItemQty = qtySum,
        customerKind = customerKind,
        customers = customerRows,
        selectedCustomerId = selectedCustomerId,
    )
}
