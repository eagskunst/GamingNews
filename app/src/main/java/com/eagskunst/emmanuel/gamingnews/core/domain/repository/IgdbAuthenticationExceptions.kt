package com.eagskunst.emmanuel.gamingnews.core.domain.repository

/** Failure to acquire a Twitch OAuth token for IGDB. */
class IgdbTokenAcquisitionException(cause: Throwable) : Exception(cause)

/** IGDB rejected the token even after a renewal retry. */
class IgdbRejectedTokenException(cause: Throwable) : Exception(cause)
