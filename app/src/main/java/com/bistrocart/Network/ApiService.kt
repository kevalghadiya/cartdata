package com.retailer.Network

import com.bistrocart.login_module.PaymentModule.PhonePeUrlReq
import com.bistrocart.login_module.PaymentModule.PhonePeUrlRes
import com.bistrocart.login_module.model.req.LoginReq
import com.bistrocart.login_module.model.res.LoginRes
import com.bistrocart.login_module.model.res.ProfileRes
import com.bistrocart.login_module.model.res.SignUpRes
import com.bistrocart.main_module.model.req.*
import com.bistrocart.main_module.model.res.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login")
    fun login(@Body loginReq: LoginReq): Observable<Response<LoginRes>>

    @Multipart
    @POST("signup")
    fun getSignUp(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part("outlet_name") outlet_name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("location") location: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("long") long: RequestBody,
        @Part("city_id") city_id: RequestBody,
        @Part("pincode") pincode: RequestBody,
        @Part("pan_number") pan_number: RequestBody,
        @Part("document") document: RequestBody,
        @Part("doc_number") doc_number: RequestBody,
        @Part("device_type") device_type: RequestBody,
        @Part("device_token") device_token: RequestBody,
        @Part doc_image: MultipartBody.Part?,
        @Part pan_image:MultipartBody.Part?,
    ): Observable<Response<SignUpRes>>

    @GET("city_list")
    fun getCityList():Observable<Response<CityListRes>>

    @GET("main_category_list")
    fun getCategoryList(): Observable<Response<CategoryRes>>

    @POST("sub_category_list")
    fun getSubCategoryList(@Body categoryMainSubReq: CategoryMainSubReq): Observable<Response<CategoryMainSubRes>>

    @POST("product_list")
    fun getProductList(@Query("page") page:String, @Body productReq: ProductReq): Observable<Response<ProductListRes>>

    @POST("signup_otp")
    fun getSignUpOTP(@Query("phone_number") phone_number:String): Observable<Response<BasicRes>>

    @POST("add_cart_items")
    fun addToCart(@Body addToCartReq:AddToCartReq):Observable<Response<BasicRes>>

     @POST("add_cart_items")
    fun addToCartNew(@Body addToCartReq:AddToCartReq):Observable<Response<MyCartRes>>

     @POST("deal_ofthe_day")
    fun getDealOfTheDay():Observable<Response<ProductListRes>>

    @GET("my_cart_list")
    fun myCartList():Observable<Response<MyCartRes>>

    @GET("time_slot_list")
    fun timeSlot(): Observable<Response<TimeSloteRes>>

    @POST("place_order")
    fun placeOrder(@Body placeOrderReq: PlaceOrderReq): Observable<Response<PlaceOrderRes>>

    @GET("my_orderList")
    fun myOrderList(@Query("page") page:String): Observable<Response<OrderListRes>>

    @GET("favourite_list")
    fun favouriteList(): Observable<Response<ProductListRes>>

    //add or edit
     @POST("add_favourite")
     fun favourAddEdit(@Query("product_id") product_id:String,@Query("favourite") favourite:String): Observable<Response<FavouriteAddEditRes>>

     //orderDetails
    @POST("orderDetail")
    fun orderDetails(@Body orderDetailsReq: OrderDetailsReq):Observable<Response<OrderDetailsRes>>

    //promocode
    @POST("apply_promocode")
    fun applyPromoCode(@Query("promocode") promocode:String,@Query("amount") amount:String): Observable<Response<BasicRes>>

    //cancel order
    @POST("cancel_order")
    fun cancelOrder(@Query("order_id") order_id:String): Observable<Response<BasicRes>>

    //order return
    @POST("return_order")
    fun returnOrder(@Query("order_id") order_id:String,@Query("reason") reason:String,@Query("status") status:String): Observable<Response<BasicRes>>

    //notification list
    @GET("notification_list")
    fun notificationList(): Observable<Response<NotificationRes>>

    @GET("product_search")
    fun getProductSearch(@Query("query") searchText:String?): Observable<Response<ProductListRes>>

    @GET("my_profilePage")
    fun getProfile(): Observable<Response<ProfileRes>>

    @POST("update_profile")
    fun getUpdateProfile(@Body updateMyProfile: UpdateMyProfileReq): Observable<Response<BasicRes>>

    @POST("get_phonepeUrl")
    fun getPhonePeUrlWeb(@Body phonePeUrlReq: PhonePeUrlReq): Observable<Response<PhonePeUrlRes>>
}

