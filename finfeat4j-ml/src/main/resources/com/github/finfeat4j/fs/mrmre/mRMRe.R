# Install required packages (if not already installed)
if (!requireNamespace("data.table", quietly = TRUE)) install.packages("data.table")
if (!requireNamespace("mRMRe", quietly = TRUE)) install.packages("mRMRe")

# Load libraries
library(data.table)
library(mRMRe)

# Function to perform feature selection
feature_selection <- function(file_path, chunk_size, per_chunk_size, mi_threshold_chunk = -Inf, causality_threshold_chunk = Inf, mi_threshold_final = -Inf, causality_threshold_final = Inf) {
  # Load dataset using fread
  data.cgps <- fread(file_path)

  # Ensure all data is numeric
  data.cgps[] <- lapply(data.cgps, function(x) {
    num_x <- as.numeric(as.character(x))
    if (any(is.na(num_x))) {
      warning("Non-numeric values found and converted to NA.")
    }
    return(num_x)
  })

  # Identify the class column index (assuming it's named 'class')
  class_col_index <- which(colnames(data.cgps) == "class")

  # Check if class column exists
  if (length(class_col_index) == 0) {
    stop("Class column named 'class' not found in the dataset.")
  }

  # Total number of features (excluding the class column)
  total_features <- ncol(data.cgps) - 1
  num_chunks <- ceiling(total_features / chunk_size)

  # Empty lists to store selected features from each chunk
  selected_features <- list()
  selected_features_indexes <- list()

  # Process features in chunks
  for (i in 1:num_chunks) {
    # Determine the range of feature indices for this chunk
    start_index <- (i - 1) * chunk_size + 1
    end_index <- min(i * chunk_size, total_features)

    # Print debug info for current chunk
    cat("Processing chunk:", i, "Start index:", start_index, "End index:", end_index, "\n")

    # Create feature indices vector (note: skip class column)
    feature_indices <- setdiff(start_index:end_index, class_col_index)

    # Debug info: show feature indices to be used
    # cat("Feature indices for chunk:", feature_indices, "\n")

    # Create a subset of the data with the class and chunk of features
    chunk_data <- data.cgps[, unique(c(class_col_index, feature_indices)), with = FALSE]

    # Check if chunk_data is empty
    if (ncol(chunk_data) == 0) {
      warning("No features selected in chunk:", i)
      next
    }

    # Create mRMR dataset for the chunk
    dd_chunk <- mRMR.data(data = chunk_data)

    # Perform mRMR feature selection on the chunk
    result <- mRMR.ensemble(data = dd_chunk, target_indices = c(1), solution_count = 1, feature_count = min(per_chunk_size, length(feature_indices)))

    # Store the selected features from the chunk
    sol <- solutions(result, mi_threshold = mi_threshold_chunk, causality_threshold = causality_threshold_chunk)
    selected_features_indexes[[i]] <- unique(na.omit(unlist(sol[[1]])))
    selected_features[[i]] <- result@feature_names[selected_features_indexes[[i]]]
  }

  # Combine selected features from all chunks
  selected_features_combined <- unique(unlist(selected_features))

  selected_features_indexes_combined <- which(colnames(data.cgps) %in% selected_features_combined)

  # Debug info: show combined selected features
  # cat("Selected features combined:", selected_features_combined, "\n")

  # Perform final feature selection on the combined features
  if (length(selected_features_indexes_combined) > 0) {
    final_data <- data.cgps[, unique(c(class_col_index, selected_features_indexes_combined)), with = FALSE]

    # Check if final_data is not empty
    if (ncol(final_data) > 0) {
      final_dd <- mRMR.data(data = final_data)
      final_result <- mRMR.ensemble(data = final_dd, target_indices = c(1), solution_count = 1, feature_count = min(2000, length(selected_features_combined)))

      # Store the final selected features
      # sol <- solutions(final_result, causality_threshold = -0.001, mi_threshold = 0.005)
      sol <- solutions(final_result, causality_threshold = causality_threshold_final, mi_threshold = mi_threshold_final)
      final_selected_features <- final_result@feature_names[na.omit(unlist(sol[[1]]))]
      return(list(features = unlist(final_selected_features), scores = unlist(scores(final_result))))
    } else {
      warning("Final data is empty; no features selected.")
      return(NULL)
    }
  } else {
    warning("No features selected; returning NULL.")
    return(NULL)
  }
}