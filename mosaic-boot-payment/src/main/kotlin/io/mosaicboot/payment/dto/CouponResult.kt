package io.mosaicboot.payment.dto

import io.mosaicboot.payment.db.entity.PaymentCoupon
import io.mosaicboot.payment.db.entity.PaymentGoods
import java.math.BigDecimal

sealed class CouponResult(
    val coupon: PaymentCoupon,
    val goods: PaymentGoods,
) {
    class AlreadyUsed(
        coupon: PaymentCoupon,
        goods: PaymentGoods
    ) : CouponResult(coupon, goods)

    class SoldOut(
        coupon: PaymentCoupon,
        goods: PaymentGoods,
    ) : CouponResult(coupon, goods)

    class Usable(
        coupon: PaymentCoupon,
        goods: PaymentGoods,
        val appliedAmounts: List<BigDecimal>,
    ) : CouponResult(coupon, goods)
}