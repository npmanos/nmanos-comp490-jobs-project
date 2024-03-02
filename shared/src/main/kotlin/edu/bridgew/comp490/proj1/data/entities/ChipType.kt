package edu.bridgew.comp490.proj1.data.entities

import com.squareup.moshi.Json
import java.io.Serializable

enum class ChipType(val param: String) : Serializable {
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
