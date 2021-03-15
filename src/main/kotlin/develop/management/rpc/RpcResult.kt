package develop.management.rpc

import reactor.core.publisher.Flux

data class RpcResult(val stream: Flux<RpcReply>, val corrID: Long)