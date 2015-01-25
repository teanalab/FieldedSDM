library(jsonlite)
library(lattice)

extract.collection.name <- function(filename) {
    strsplit(basename(filename), "\\.")[[1]][1]
}
extract.attributes <- function(jsonString) {
    fromJSON(paste('{',jsonString,'}'))
}

for (file in c("unigrams", "od", "unw")) {
    csv <- read.csv(paste(file, ".csv", sep=""), header = FALSE, col.names=c("filename", "weights.json"),
                    stringsAsFactors = FALSE)[c(1:5,7:11,13:17,19:23),]

    data <- do.call(rbind.data.frame,lapply(csv$weights.json, extract.attributes))
    row.names(data) <- NULL
    data <- data.frame(t(apply(data, 1, function(x){x/sum(x)})))
    data$collection <- sapply(csv$filename, extract.collection.name)
    data <- data[c(6,1:5)]
    data <- data.frame(do.call(rbind, by(data[, 2:6], data[, 1], colMeans)))
    names(data) <- sapply(names(data), function(n){tail(strsplit(n, "\\.")[[1]],n=1)})
    data$group <- as.factor(row.names(data))
    levels(data$group) <- c("SemSearch_ES", "ListSearch", "INEX_LD", "QALD2")
    cairo_ps(paste(file, ".eps", sep=""))
    formula <- attributes + categories + names + outgoingentitynames + similarentitynames ~ group
    print(stripplot(formula, data=data, ylab = "average field weights",
              par.settings = list(superpose.symbol = list(pch = 1:5)), auto.key = list(space = "right",
                                                                           border=TRUE, padding.text=4)))
    graphics.off()
}
