package de.robolab.server.auth

open class User(val permissions: Set<Permission>) {
    object Empty : User(emptySet())
}