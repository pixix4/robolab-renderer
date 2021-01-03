package de.robolab.client.app.repository

actual class DatabaseMessageStorage actual constructor() : IMessageStorage by MemoryMessageStorage()
