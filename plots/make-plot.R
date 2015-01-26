library(jsonlite)
library(lattice)

extract.collection.name <- function(filename) {
    strsplit(basename(filename), "\\.")[[1]][1]
}
extract.attributes <- function(jsonString) {
    fromJSON(paste('{',jsonString,'}'))
}

for (file in c("unigrams", "od", "unw", "fsdm", "sdm")) {
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
    if (file == "sdm") {
        formula <- uniw + odw + uww ~ group
        text <- c(expression(lambda[T]),expression(lambda[O]),expression(lambda[U]))
        ylim <- c(-0.02,0.9)
        ylab <- expression(paste(lambda[T], ", ", lambda[O], ", ", lambda[U]))
    } else if (file == "fsdm") {
        formula <- CONST + OD_CONST + UNW_CONST ~ group
        text <- c(expression(lambda[T]),expression(lambda[O]),expression(lambda[U]))
        ylim <- c(-0.02,0.9)
        ylab <- expression(paste(lambda[T], ", ", lambda[O], ", ", lambda[U]))
    } else {
        formula <- names + attributes + categories + similarentitynames + outgoingentitynames ~ group
        text <- c("names", "attributes", "categories", "similar entity names",
                  "related entity names")
        ylim <- c(-0.02,0.6)
        ylab <- "average field weights"
    }
    cairo_ps(paste(file, ".eps", sep=""), height=5)
    print(stripplot(formula, data=data, ylab = ylab, ylim=ylim,
                    par.settings = list(superpose.symbol = list(pch = 1:5, cex=1.5)),
                    auto.key = list(space = "right", border=TRUE, padding.text=4,
                                    text=text), scales=list(font=2)))
    graphics.off()
}
