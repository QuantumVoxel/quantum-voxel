//
// Created by quint on 29/08/2025.
//

#include <iostream>
#include <memory>
#include <ostream>
#include <ultreon_jv/quantum_client/QuantumClient.cpp>

using namespace ultreon_jv::quantum_client;

int main(int argc, char *argv[]) {
    std::shared_ptr<QuantumClient> client = QuantumClient::get();
    client->world;
}

