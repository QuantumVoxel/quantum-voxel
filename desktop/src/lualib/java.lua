---@diagnostic disable: missing-return
---@class java
java = {}

---@class jobject
jobject = {}

---@class jarray
jarray = {}

---@param name string
---@return any
function java.import(name) end

---@param class any
---@param name string
function java.method(class, name) end

---@param class any
---@param dim number
---@param ... number
---@return jarray
function java.array(class, dim, ...) end

---@return jobject|nil
function java.caught() end

---@param class any
---@return jobject
function java.new(class) end

---@param message string
function print(message) end

error("Can't require Java lib directly!")
