library(jsonlite)
library(lattice)

extract.collection.name <- function(filename) {
  strsplit(basename(filename), "\\.")[[1]][1]
}
extract.attributes <- function(jsonString) {
  fromJSON(paste('{',jsonString,'}'))
}

csv <- read.csv("unigrams.csv", header = FALSE, col.names=c("filename", "weights.json"),
                stringsAsFactors = FALSE)[c(1:5,7:11,13:17,19:23),]

data <- do.call(rbind.data.frame,lapply(csv$weights.json, extract.attributes))
row.names(data) <- NULL
data <- data.frame(t(apply(data, 1, function(x){x/sum(x)})))
data$collection <- sapply(csv$filename, extract.collection.name)
data <- data[c(6,1:5)]
data <- data.frame(do.call(rbind, by(data[, 2:6], data[, 1], colMeans)))
data$group <- as.factor(row.names(data))
levels(data$group) <- c("SemSearch_ES", "ListSearch", "INEX_LD", "QALD2")
cairo_ps("unigrams.eps")
stripplot(uni.attributes + uni.categories + uni.names + uni.outgoingentitynames +
            uni.similarentitynames ~ group, data=data, pch=c(1, 2, 3, 4, 5), 
          ylab = "Field weight")
dev.off()



