using Microsoft.AspNetCore.Mvc;
using Product.DTOs;
using Product.Services;

namespace Product.Controllers;

[ApiController]
[Route("api/v1/product")]
public class ProductController : ControllerBase
{
    private readonly IProductService _productService;
    private readonly IBrandService _brandService;

    public ProductController(IProductService productService, IBrandService brandService)
    {
        _productService = productService;
        _brandService = brandService;
    }

    /// <summary>
    /// Create a new product
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<ProductResponse>> CreateProduct([FromBody] ProductRequest request)
    {
        var response = await _productService.CreateProductAsync(request);
        return CreatedAtAction(nameof(GetProductById), new { id = response.Id }, response);
    }

    /// <summary>
    /// Get product by ID
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<ProductResponse>> GetProductById(string id)
    {
        var product = await _productService.GetProductByIdAsync(id);
        if (product == null)
        {
            return NotFound(new { message = "Product not found" });
        }
        return Ok(product);
    }

    /// <summary>
    /// Get all products with pagination and filtering
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<PaginatedResponse<ProductResponse>>> GetAllProducts(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] string? search = null,
        [FromQuery] string? category = null,
        [FromQuery] string? brandId = null)
    {
        var products = await _productService.GetAllProductsAsync(page, pageSize, search, category, brandId);
        return Ok(products);
    }

    /// <summary>
    /// Update product
    /// </summary>
    [HttpPut("{id}")]
    public async Task<ActionResult<ProductResponse>> UpdateProduct(string id, [FromBody] ProductRequest request)
    {
        var product = await _productService.UpdateProductAsync(id, request);
        if (product == null)
        {
            return NotFound(new { message = "Product not found" });
        }
        return Ok(product);
    }

    /// <summary>
    /// Delete product
    /// </summary>
    [HttpDelete("{id}")]
    public async Task<ActionResult> DeleteProduct(string id)
    {
        var deleted = await _productService.DeleteProductAsync(id);
        if (!deleted)
        {
            return NotFound(new { message = "Product not found" });
        }
        return NoContent();
    }

    // Brand endpoints

    /// <summary>
    /// Create a new brand
    /// </summary>
    [HttpPost("brand")]
    public async Task<ActionResult<BrandResponse>> CreateBrand([FromBody] BrandRequest request)
    {
        var response = await _brandService.CreateBrandAsync(request);
        return CreatedAtAction(nameof(GetBrandById), new { id = response.Id }, response);
    }

    /// <summary>
    /// Get brand by ID
    /// </summary>
    [HttpGet("brand/{id}")]
    public async Task<ActionResult<BrandResponse>> GetBrandById(string id)
    {
        var brand = await _brandService.GetBrandByIdAsync(id);
        if (brand == null)
        {
            return NotFound(new { message = "Brand not found" });
        }
        return Ok(brand);
    }

    /// <summary>
    /// Get all brands
    /// </summary>
    [HttpGet("brand")]
    public async Task<ActionResult<List<BrandResponse>>> GetAllBrands()
    {
        var brands = await _brandService.GetAllBrandsAsync();
        return Ok(brands);
    }
}
