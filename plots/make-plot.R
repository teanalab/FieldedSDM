library(jsonlite)
library(lattice)

extract.collection.name <- function(filename) {
    strsplit(basename(filename), "\\.")[[1]][1]
}
extract.attributes <- function(jsonString) {
    fromJSON(paste('{',jsonString,'}'))
}

for (file in c("unigrams", "od", "unw")) {#, "fsdm")) {
    csv <- read.csv(paste(file, ".csv", sep=""), header = FALSE,
                    col.names=c("filename", "weights.json"),
                    stringsAsFactors = FALSE)[c(1:5,7:11,13:17,19:23),]

    data <- do.call(rbind.data.frame,lapply(csv$weights.json, extract.attributes))
    row.names(data) <- NULL
    data <- data.frame(t(apply(data, 1, function(x){x/sum(x)})))
    data$collection <- sapply(csv$filename, extract.collection.name)
    ncol <- dim(data)[2]
    data <- data[c(ncol,1:ncol-1)]
    data <- data.frame(do.call(rbind, by(data[, 2:ncol], data[, 1], colMeans)))
    names(data) <- sapply(names(data), function(n){tail(strsplit(n, "\\.")[[1]],n=1)})
    data$group <- factor(row.names(data),
                         levels=c("SemSearch_ES", "ListSearch", "INEX_LD", "QALD2"))
    if (file == "fsdm") {
        formula <- a ~ b
    } else {
        formula <- names + attributes + categories + similarentitynames + outgoingentitynames ~ group
        text = c("names", "attributes", "categories", "similar entity names",
            "related entity names")
    }
    cairo_ps(paste(file, ".eps", sep=""))
    print(stripplot(formula, data=data, ylab = "average field weights",
                    par.settings = list(superpose.symbol = list(pch = 1:5)),
                    auto.key = list(space = "right", border=TRUE, padding.text=4,
                                    text=text)))
    graphics.off()
    ## cairo_ps(paste(file, ".rescale.eps", sep=""))
    ## print(stripplot(formula, data=data, ylab = "average field weights", ylim=0:1,
    ##                 par.settings = list(superpose.symbol = list(pch = 1:5)),
    ##                 auto.key = list(space = "right", border=TRUE, padding.text=4)))
    ## graphics.off()
}
