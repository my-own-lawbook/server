package me.bumiller.mol.model.config

import kotlin.time.Duration

/**
 * Config bundle for the app
 */
data class AppConfig(

    /**
     * The JWT signing secret
     */
    val jwtSecret: String,

    /**
     * How long before a jwt expires
     */
    val jwtDuration: Duration,

    /**
     * How long before a refresh token expires
     */
    val refreshDuration: Duration,

    /**
     * How long before an email token expires
     */
    val emailTokenDuration: Duration,

    /**
     * URL to the SQL database
     */
    val databaseUrl: String,

    /**
     * User for the database
     */
    val databaseUser: String,

    /**
     * Password to the database
     */
    val databasePassword: String

)