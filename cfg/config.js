/// android ///
with(createBot("android")) {
    name				= "Mojo"
    login				= "Mojo"
    commandPrefix		= "::"
    voidCommandPrefix	= ";;"
    messageDelay		= 2000
    connect				( "irc.androidirc.org", 6667 )
}

/// foco ///
with(createBot("foco")) {
    name				= "Mojo"
    login				= "Mojo"
    commandPrefix		= "::"
    voidCommandPrefix	= ";;"
    messageDelay		= 2000
    connectSSL			( "irc.foco.org", 6697 )
}