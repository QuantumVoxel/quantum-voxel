"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = default_1;
// noinspection JSUnusedGlobalSymbols
function default_1(_common, platform, di) {
    let logger = platform.getLogger();
    logger.info("Loading test script!");
    if (platform.isClient()) {
        const client = di.resolve("client.QuantumClient");
        if (!(client === null || client === void 0 ? void 0 : client.world)) {
            logger.info("No client or world found yet!");
        }
    }
}
