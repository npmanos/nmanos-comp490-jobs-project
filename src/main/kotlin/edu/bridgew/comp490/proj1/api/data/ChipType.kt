package edu.bridgew.comp490.proj1.api.data

import com.squareup.moshi.Json

enum class ChipType(val param: String) {
    Title("job_family_1"),
    Location("city"),

    @Json(name = "Date posted")
    DatePosted("date_posted"),
    Requirements("requirements"),
    Type("employment_type"),

    @Json(name = "Company type")
    CompanyType("industry.id"),
    Employer("organization_mid"),
}

class ChipTypeJsonAdapter
