package dev.ultreon.quantum.network.system

class ReadOnlyConnectionException : RuntimeException {
  constructor() : super("Connection is read-only")

  constructor(message: String?) : super(message)
}
