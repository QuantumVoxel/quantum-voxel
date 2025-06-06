var r = await fetch("/libraries/libgdx.so");
var buf = await r.arrayBuffer();
export default
{
	wasmModule: buf
}