package com.smileidentity.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServicesResponse(
    @Json(name = "bank_codes") val bankCodes: List<BankCode>,
    // TODO: There is an id_types key. Country -> ID type -> array of fields. what do the fields
    //  represent? i.e. PHONE_NUMBER has country, user_id, first_name, last_name as fields. Are
    //  these expected response fields?
    @Json(name = "hosted_web") val hostedWeb: HostedWeb,
)

@JsonClass(generateAdapter = true)
data class BankCode(
    @Json(name = "name") val name: String,
    @Json(name = "code") val code: Int,
)

/**
 * The key is the 2 letter ISO country code for the country. The key should be copied in to
 * [CountryInfo.countryCode]
 */
private typealias CountryCodeToCountryInfo = Map<String, CountryInfo>

/**
 * The key is the unique identifier for the ID type. The key should be copied in to
 * [AvailableIdType.idTypeKey]
 */
private typealias IdTypeKeyToAvailableIdType = Map<String, AvailableIdType>

@JsonClass(generateAdapter = true)
data class HostedWeb(
    @Json(name = "basic_kyc") internal val _basicKyc: CountryCodeToCountryInfo,
    @Json(name = "biometric_kyc") internal val _biometricKyc: CountryCodeToCountryInfo,
    @Json(name = "enhanced_kyc") internal val _enhancedKyc: CountryCodeToCountryInfo,
    @Json(name = "doc_verification") internal val _docVerification: CountryCodeToCountryInfo,
    @Json(name = "ekyc_smartselfie") internal val _enhancedKycSmartSelfie: CountryCodeToCountryInfo,
) {
    val basicKyc = _basicKyc.toCountryInfo()
    val biometricKyc = _biometricKyc.toCountryInfo()
    val enhancedKyc = _enhancedKyc.toCountryInfo()
    val docVerification = _docVerification.toCountryInfo()
    val enhancedKycSmartSelfie = _enhancedKycSmartSelfie.toCountryInfo()

    /**
     * This is used to convert the [Map] type response to a single object, which makes for a better
     * data model representation
     */
    private fun CountryCodeToCountryInfo.toCountryInfo() = map {
        it.value.copy(countryCode = it.key)
    }
}

/**
 * The [countryCode] field is not populated/returned by the API response, hence it being marked as
 * [Transient]. However, it should be populated before usage of this class.
 */
@JsonClass(generateAdapter = true)
data class CountryInfo(
    @Transient val countryCode: String = "",
    @Json(name = "name") val name: String,
    @Json(name = "id_types") internal val _availableIdTypes: IdTypeKeyToAvailableIdType,
) {
    val availableIdTypes = _availableIdTypes.toAvailableIdTypes()

    private fun IdTypeKeyToAvailableIdType.toAvailableIdTypes() = map {
        it.value.copy(idTypeKey = it.key)
    }
}

/**
 * The [idTypeKey] is not populated by the API response, hence being marked as [Transient]. However,
 * it should be populated before usage of this class.
 *
 * [testData], in practice, is only null for BANK_ACCOUNT ID Types
 */
@JsonClass(generateAdapter = true)
data class AvailableIdType(
    @Transient val idTypeKey: String = "",
    @Json(name = "label") val label: String,
    @Json(name = "required_fields") val requiredFields: List<String> = emptyList(),
    @Json(name = "test_data") val testData: String?,
    // Don't use a Regex object here directly as that requires us to compile the pattern, which is a
    // heavy operation
    @Json(name = "id_number_regex") val idNumberRegex: String?,
)
