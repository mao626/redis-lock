local vKey=KEYS[1]
local oldValue=KEYS[2]
local newValue=KEYS[3]
if redis.call("EXISTS", vKey) == 1 then
	local dbValue = redis.call("GET", vKey)
	if dbValue == oldValue then
		redis.call("SET", vKey, newValue)
		return 1
	else
		return 0
	end
else
	return 0
end